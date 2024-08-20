import { Injectable, signal } from '@angular/core';
import {
  GenericCache,
  ICacheStore,
  Market,
  SimplifiedPlaylist,
  SpotifyApi,
  Track,
  UserProfile,
} from '@spotify/web-api-ts-sdk';
import { Song } from '../main/main.model';
import { queryStringComposer } from '../../util/QueryStringComposer';
import { scoreMatch } from '../../util/SongMatcher';
import { environment } from '../../environments/environment';

// Let the Spotify API handle refreshing OAuth token
const SDK_TOKEN_KEY = 'spotify-sdk:AuthorizationCodeWithPKCEStrategy:token';
const CLIENT_ID = environment.spotifyClientId || 'not configured';
const LIMIT = 50;

/** Copied and adapted from LocalStorageCachingStrategy of the Spotify API source code */
class SessionStorageCachingStrategy extends GenericCache {
  constructor() {
    super(new SessionStorageCacheStore());
  }
}

/** Copied and adapted from LocalStorageCachingStrategy of the Spotify API source code */
class SessionStorageCacheStore implements ICacheStore {
  public get(key: string): string | null {
    return sessionStorage.getItem(key);
  }

  public set(key: string, value: string): void {
    sessionStorage.setItem(key, value);
  }

  public remove(key: string): void {
    sessionStorage.removeItem(key);
  }
}

/**
 * See https://github.com/spotify/spotify-web-api-ts-sdk
 */
@Injectable({
  providedIn: 'root',
})
export class SpotifyService {
  /* Cache the user profile of the currently logged in user
   */
  private userProfile = signal<UserProfile | undefined>(undefined);
  ro_userProfile = this.userProfile.asReadonly();

  private spotifyApi = SpotifyApi.withUserAuthorization(
    CLIENT_ID,
    'http://localhost:4200/start',
    [
      'playlist-read-private',
      'playlist-modify-private',
      'playlist-modify',
      'user-read-private',
      'user-read-email',
    ],
    {
      //prefer session storage over (default) local storage (in case computers are shared amongst users)
      cachingStrategy: new SessionStorageCachingStrategy(),
    }
  );

  constructor() {
    // OnInit-interface does nothing on an @Injectable, so we must initialize in constructor
    console.log('CLIENT_ID=' + CLIENT_ID);
  }

  init() {
    if (this.userProfile() != null) {
      console.log('Initializing SpotifyService instance (loading profile)');
      (async () => {
        this.userProfile.set(await this.spotifyApi.currentUser.profile());
      })();
    }
  }

  async authenticate() {
    const response = await this.spotifyApi.authenticate();
    console.log(
      `Logged into Spotify: ${response.authenticated}`,
      response.accessToken
    );
    if (response.authenticated) {
      this.userProfile.set(await this.spotifyApi.currentUser.profile());
    }
    return response.authenticated;
  }

  logout() {
    this.spotifyApi.logOut();
    this.userProfile.set(undefined);
  }

  isConnected() {
    return sessionStorage.getItem(SDK_TOKEN_KEY) != null;
  }

  async getPlaylistTracks(playlist: SimplifiedPlaylist) {
    if (!playlist) {
      return undefined;
    }
    const tracks: Track[] = [];
    const market: Market = (this.ro_userProfile()?.country as Market) || 'US';
    const fields = undefined;
    let offset = 0;
    let hasMore = true;
    while (hasMore) {
      let result = await this.spotifyApi.playlists.getPlaylistItems(
        playlist.id,
        market,
        fields,
        LIMIT,
        offset
      );
      if (!result.items || result.items.length === 0) {
        hasMore = false;
      } else {
        offset += result.items.length;
        for (const item of result.items) {
          tracks.push(item.track);
        }
      }
    }
    return tracks;
  }

  async findPlaylist(
    playlistName: string
  ): Promise<SimplifiedPlaylist | undefined> {
    let offset = 0;
    let hasMore = true;
    while (hasMore) {
      let result = await this.spotifyApi.currentUser.playlists.playlists(
        LIMIT,
        offset
      );
      if (!result.items || result.items.length === 0) {
        hasMore = false;
      } else {
        offset += result.items.length;
        for (const candidate of result.items) {
          if (candidate.name === playlistName) {
            return candidate;
          }
        }
      }
    }
    return undefined;
  }

  async createPlaylist(playlistName: string) {
    const userId = this.userProfile()?.id;
    if (userId) {
      const response = await this.spotifyApi.playlists.createPlaylist(userId, {
        name: playlistName,
        public: false,
      });
      if (response.id) {
        return await this.findPlaylist(playlistName);
      }
    }
    return undefined;
  }

  async deleteFromPlaylist(
    playlist: SimplifiedPlaylist | undefined,
    track: Track
  ) {
    if (playlist) {
      await this.spotifyApi.playlists.removeItemsFromPlaylist(playlist.id, {
        tracks: [{ uri: track.uri }],
      });
    } else {
      console.log('No playlist selected');
    }
  }

  async addToPlaylist(
    playlist: SimplifiedPlaylist | undefined,
    song: Song,
    year: number
  ) {
    if (playlist) {
      const query = queryStringComposer(song, year);
      const result = await this.spotifyApi.search(
        query,
        ['track'],
        this.getMarket(),
        5
      );
      const match = this.findBestMatch(song, result.tracks.items);
      if (match) {
        await this.spotifyApi.playlists.addItemsToPlaylist(playlist.id, [
          match.uri,
        ]);
      }
      return match;
    } else {
      console.log('No playlist selected');
    }
    return undefined;
  }

  getPlaylistName(year: number) {
    return year + ' charted songs';
  }

  private getMarket(): Market {
    return (this.ro_userProfile()?.country as Market) || 'US';
  }

  private findBestMatch(song: Song, tracks: Track[]) {
    let highestScore = 0;
    let bestMatch = undefined;
    for (const track of tracks) {
      const matchScore = scoreMatch(song, track);
      const thisScore = matchScore[0] + matchScore[1];
      //only consider tracks that meet minimal likeliness ( > 75 points)
      if (
        thisScore > highestScore &&
        matchScore[0] > 75 &&
        matchScore[1] > 75
      ) {
        highestScore = thisScore;
        bestMatch = track;
      }
    }
    return bestMatch;
  }
}
