import { Component, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { ProfileComponent } from '../spotify/profile/profile.component';
import { Song } from './main.model';
import { HttpClient } from '@angular/common/http';
import { SimplifiedPlaylist, Track } from '@spotify/web-api-ts-sdk';
import { SpotifyService } from '../spotify/spotify.service';
import { matchesTrack } from '../../util/SongMatcher';

/**
 * This component is capable of loading the charts of a certain year,
 * creating a spotify playlist if not already exist, load items from
 * that playlist, match playlist items with chart items, to determine
 * which chart items are missing from the playlist.
 */
@Component({
  selector: 'app-main',
  standalone: true,
  imports: [ProfileComponent],
  templateUrl: './main.component.html',
  styleUrl: './main.component.scss',
})
export class MainComponent {
  isLoadingCharts = signal(false);
  isLoadingPlaylist = signal(false);
  isPlaylistMatched = signal(false);
  releaseYear = signal<number | undefined>(undefined);
  charts = signal<Song[]>([]);
  playlist = signal<SimplifiedPlaylist | undefined>(undefined);
  unmatchedFromPlaylist = signal<Track[]>([]);
  destroyRef = inject(DestroyRef);

  get years(): number[] {
    const currentYear = new Date().getFullYear();
    return [...Array(currentYear - 1965 + 1).keys()].map((i) => i + 1965);
  }
  constructor(
    private httpClient: HttpClient,
    private spotifyService: SpotifyService
  ) {
    console.log('MainComponent created');
    this.spotifyService.init();
  }

  getReleases(year: string) {
    this.releaseYear.set(Number.parseInt(year));
    this.isLoadingCharts.set(true);
    const subscription = this.httpClient
      .get<Song[]>('/api/public/releases/' + year)
      .subscribe({
        next: (response) => this.charts.set(response),
        complete: () => {
          this.isLoadingCharts.set(false);
          this.unmatchedFromPlaylist.set([]);
          this.isPlaylistMatched.set(false);
        },
      });
    this.destroyRef.onDestroy(() => subscription.unsubscribe());
  }

  async onMatchCurrentPlaylist() {
    if (!this.releaseYear()) {
      console.log('No release year selected...');
      return;
    }

    this.isLoadingPlaylist.set(true);
    let playlist = await this.spotifyService.findPlaylist(
      this.spotifyService.getPlaylistName(this.releaseYear()!)
    );

    if (!playlist) {
      playlist = await this.spotifyService.createPlaylist(
        this.spotifyService.getPlaylistName(this.releaseYear()!)
      );
    }

    if (playlist) {
      let tracks = await this.spotifyService.getPlaylistTracks(playlist);

      if (tracks) {
        let updatedChartItems: Song[] = [];
        for (const chartItem of this.charts()) {
          for (const [index, track] of tracks.entries()) {
            if (matchesTrack(chartItem, track)) {
              chartItem.match = track;
              tracks.splice(index, 1);
              break;
            }
          }
          updatedChartItems.push(chartItem);
        }

        //sort updatedChartItems so that matched chartitems are at the top and unmatched chartitems are at the bottom
        updatedChartItems = updatedChartItems.sort((a, b) => {
          if (a.match && !b.match) return -1;
          if (!a.match && b.match) return 1;
          return 0;
        });
        this.isPlaylistMatched.set(true);
        this.charts.set(updatedChartItems);
        this.unmatchedFromPlaylist.set(tracks);
        this.playlist.set(playlist);
        this.isPlaylistMatched.set(true);
      }
    }
    this.isLoadingPlaylist.set(false);
  }

  async deleteFromPlaylist(track: Track) {
    const index = this.unmatchedFromPlaylist().indexOf(track);
    if (index > -1) {
      console.log(`Delete track at index ${index}: `, track);
      const updatedList = [...this.unmatchedFromPlaylist()];
      updatedList.splice(index, 1);
      this.unmatchedFromPlaylist.set(updatedList);
      await this.spotifyService.deleteFromPlaylist(this.playlist(), track);
    }
  }

  async addToPlaylist(song: Song) {
    if (!this.releaseYear()) {
      console.log('No release year selected...');
      return;
    }
    const match = await this.spotifyService.addToPlaylist(
      this.playlist(),
      song,
      this.releaseYear()!
    );
    if (match) {
      console.log('Found match: ', match);
      const index = this.charts().indexOf(song);
      if (index > -1) {
        const updatedCharts = [...this.charts()];
        updatedCharts[index] = { ...song, match };
        this.charts.set(updatedCharts);
      }
    } else {
      console.log('Not found on Spotify');
    }
  }

  /**
   * Action that will go over all releases that have not yet a match in related playlist and try to
   * find the Spotify track and add it to the playlist. When there is no related playlist yet, it wil
   * be created.
   */
  async resolveAll() {
    let matchCounter = 0;
    for (const [index, song] of this.charts().entries()) {
      if (!song.match) {
        await this.addToPlaylist(song);
        if (this.charts()[index].match) {
          matchCounter++;
        }
      }
    }
    console.log(
      `Finished resolving all songs and found ${matchCounter} matches.`
    );
  }
}
