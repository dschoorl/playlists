/*
 * Copyright 2018 Red Star Development.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package info.rsdev.playlists.services;

import static info.rsdev.playlists.spotify.QueryStringComposer.makeQueryString;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.exceptions.detailed.TooManyRequestsException;
import com.wrapper.spotify.model_objects.special.SnapshotResult;
import com.wrapper.spotify.model_objects.specification.Paging;
import com.wrapper.spotify.model_objects.specification.Playlist;
import com.wrapper.spotify.model_objects.specification.PlaylistSimplified;
import com.wrapper.spotify.model_objects.specification.Track;
import com.wrapper.spotify.model_objects.specification.User;
import com.wrapper.spotify.requests.data.playlists.AddTracksToPlaylistRequest;
import com.wrapper.spotify.requests.data.playlists.CreatePlaylistRequest;
import com.wrapper.spotify.requests.data.search.simplified.SearchTracksRequest;
import com.wrapper.spotify.requests.data.users_profile.GetCurrentUsersProfileRequest;

import info.rsdev.playlists.domain.CatalogPlaylist;
import info.rsdev.playlists.domain.Song;
import info.rsdev.playlists.domain.SongFromCatalog;
import info.rsdev.playlists.spotify.PlaylistIterator;
import info.rsdev.playlists.spotify.PlaylistTrackIterator;
import info.rsdev.playlists.spotify.QueryCache;
import info.rsdev.playlists.spotify.TooManyRequestsExceptionHandler;

public class SpotifyCatalogService implements MusicCatalogService {
	
    private static final Logger LOGGER = LoggerFactory.getLogger(SpotifyCatalogService.class);
    
    private static final int SEGMENT_SIZE = 50;
    
    private final QueryCache queryCache;

    private final SpotifyApi spotifyApi;
    
    private final User currentUser;

    public SpotifyCatalogService(String clientId, String clientSecret, String accessToken, String refreshToken) {
        spotifyApi = SpotifyApi.builder()
                .setClientId(clientId)
                .setClientSecret(clientSecret)
                .setAccessToken(accessToken)
                .setRefreshToken(refreshToken)
                .build();
        
        this.currentUser = getCurrentUser();
        
        this.queryCache = new QueryCache();
        LOGGER.info(String.format("Read %d cache entries from file", this.queryCache.size()));
    }

	@Override
	public Optional<SongFromCatalog> findSong(Song song) {
	    try {
	    	String query = makeQueryString(song);
	    	Optional<SongFromCatalog> result = queryCache.getFromCache(query);
	    	if (!result.isPresent()) {
	    		result = searchSpotifyForSong(song, query);
	    		//do not cache nulls when song is not found on spotify, since I am trying to improve my search query
	    		result.ifPresent(spotifySong ->  queryCache.cache(query, spotifySong));
	    		if (LOGGER.isDebugEnabled() && !result.isPresent()) {
	    			LOGGER.debug(String.format("Found=%b: %s with q='%s'", result.isPresent(), song, query));
	    		}
			}
	    	return result;
    	} catch (SpotifyWebApiException | IOException e) {
    		throw new RuntimeException(e);
    	}
	}
	
    private Optional<SongFromCatalog> searchSpotifyForSong(Song song, String queryString) throws IOException, SpotifyWebApiException {
    	Paging<Track> searchResult = executeSearchOnSpotify(queryString);
		int hits = searchResult.getTotal();
		if (hits == 0) {
			return Optional.empty();
		} else {
			return selectRightResult(song, searchResult);
		}
    }
    
    private Paging<Track> executeSearchOnSpotify(String queryString) throws SpotifyWebApiException, IOException {
		SearchTracksRequest searchRequest = spotifyApi.searchTracks(queryString).build();
		Paging<Track> searchResult = null;
		while (searchResult == null) {
			try {
				searchResult = searchRequest.execute();
			} catch (TooManyRequestsException e) {
				TooManyRequestsExceptionHandler.handle(LOGGER, searchRequest.getClass().getSimpleName(), e);
			}
		}
		return searchResult;
    }
    
	private Optional<SongFromCatalog> selectRightResult(Song song, Paging<Track> searchResult) {
		//select track with highest popularity score
		Track mostPopularTrack = null;
		for (Track candidate : searchResult.getItems()) {
			if ((mostPopularTrack == null) || (mostPopularTrack.getPopularity() < candidate.getPopularity())) {
				mostPopularTrack = candidate;
			}
		}
		return Optional.ofNullable(makeSongFromCatalog(song, mostPopularTrack));
	}

	@Override
	public CatalogPlaylist getOrCreatePlaylist(String playlistName) {
		return findPlaylistWithName(playlistName)
				.orElseGet(() -> createPlaylistWithName(playlistName));
	}
	
	private Optional<CatalogPlaylist> findPlaylistWithName(String targetName) {
		PlaylistIterator iterator = PlaylistIterator.create(spotifyApi);
		while (iterator.hasNext()) {
			PlaylistSimplified spotifyPlaylist = iterator.next();
			LOGGER.info(String.format("Encountered playlist '%s'", spotifyPlaylist.getName()));
			if (spotifyPlaylist.getName().equals(targetName)) {
				return Optional.of(makePlaylist(spotifyPlaylist));
			}
		}
		return Optional.empty();
	}
	
	private CatalogPlaylist createPlaylistWithName(String newPlaylistName) {
		CreatePlaylistRequest createRequest = spotifyApi.createPlaylist(currentUser.getId(), newPlaylistName).build();
		try {
			return makePlaylist(createRequest.execute());
		} catch (SpotifyWebApiException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	private CatalogPlaylist makePlaylist(PlaylistSimplified spotifyPlaylist) {
		return new CatalogPlaylist(spotifyPlaylist.getName(), spotifyPlaylist.getId());
	}

	private CatalogPlaylist makePlaylist(Playlist spotifyPlaylist) {
		return new CatalogPlaylist(spotifyPlaylist.getName(), spotifyPlaylist.getId());
	}

	private SongFromCatalog makeSongFromCatalog(Song song, Track spotifyTrack) {
		return new SongFromCatalog(song, spotifyTrack.getUri());
	}

    private SongFromCatalog makeSongFromCatalog(Track spotifyTrack) {
        String artists = Arrays.asList(spotifyTrack.getArtists()).stream()
            .map(artist -> artist.getName())
            .collect(Collectors.joining(" "));
        
        Song song = new Song(artists, spotifyTrack.getName());
        return new SongFromCatalog(song, spotifyTrack.getUri());
    }

	@Override
	public void addToPlaylist(CatalogPlaylist playlist, List<SongFromCatalog> songs) {
		List<String> trackIds = songs.stream().map(song -> song.trackUri).collect(Collectors.toList());
		
		//spotify accepts max. 100 songs in a single request
		int nrOfSegments = (trackIds.size() / SEGMENT_SIZE) + ((trackIds.size() % SEGMENT_SIZE)==0 ? 0 : 1);
		int currentSegment = 1;
		String playlistId = playlist.playlistId;
		try {
			while (currentSegment <= nrOfSegments) {
				int toIndex = currentSegment * SEGMENT_SIZE;
				int fromIndex = (currentSegment * SEGMENT_SIZE) - SEGMENT_SIZE;
				List<String> segment = trackIds.subList(fromIndex, Math.min(toIndex, trackIds.size()));
				
				SnapshotResult result = addToPlaylistOnSpotify(playlistId, segment.toArray(new String[segment.size()]));
//				playlistId = result.getSnapshotId();
				currentSegment++;
			}
		} catch (SpotifyWebApiException | IOException e) {
			throw new RuntimeException(e);
		}
	}
	
    private SnapshotResult addToPlaylistOnSpotify(String playlistId, String[] trackIds) throws SpotifyWebApiException, IOException {
		AddTracksToPlaylistRequest request = spotifyApi.addTracksToPlaylist(currentUser.getId(),playlistId, trackIds).build();
		SnapshotResult response = null;
		while (response == null) {
			try {
				response = request.execute();
			} catch (TooManyRequestsException e) {
				TooManyRequestsExceptionHandler.handle(LOGGER, request.getClass().getSimpleName(), e);
			}
		}
		return response;
    }
	
	private User getCurrentUser() {
		GetCurrentUsersProfileRequest userRequest = spotifyApi.getCurrentUsersProfile().build();
		try {
			return userRequest.execute();
		} catch (SpotifyWebApiException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Collection<SongFromCatalog> getTracksInPlaylist(CatalogPlaylist playlist) {
		PlaylistTrackIterator trackIterator = PlaylistTrackIterator.create(spotifyApi, currentUser.getId(), playlist.playlistId);
		List<SongFromCatalog> songsFromCatalog = new ArrayList<>(trackIterator.getSize());
		while(trackIterator.hasNext()) {
		    Track track = trackIterator.next().getTrack();
		    songsFromCatalog.add(makeSongFromCatalog(track));
		}
		return songsFromCatalog;
	}

	public void close() {
		LOGGER.info(String.format("Write out the memory cache of %d entries to file now...", this.queryCache.size()));
		queryCache.writeCache();
	}

}
