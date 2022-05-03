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
package info.rsdev.playlists.spotify;

import static info.rsdev.playlists.spotify.QueryStringComposer.makeQueryString;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.hc.core5.http.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.rsdev.playlists.domain.CatalogPlaylist;
import info.rsdev.playlists.domain.Song;
import info.rsdev.playlists.domain.SongFromCatalog;
import info.rsdev.playlists.services.MusicCatalogService;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.exceptions.detailed.TooManyRequestsException;
import se.michaelthelin.spotify.exceptions.detailed.UnauthorizedException;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import se.michaelthelin.spotify.model_objects.special.SnapshotResult;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.Playlist;
import se.michaelthelin.spotify.model_objects.specification.PlaylistSimplified;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.model_objects.specification.User;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeRequest;

public class SpotifyCatalogService implements MusicCatalogService {
	private static final Logger LOGGER = LoggerFactory.getLogger(SpotifyCatalogService.class);
	private static final int SEGMENT_SIZE = 50;

	private QueryCache queryCache;

	private SpotifyApi spotifyApi;

	private User currentUser;

	public SpotifyCatalogService(SpotifyApi spotifyApi, String authorizationCode) throws UnauthorizedException {
		this.spotifyApi = spotifyApi;
		authorize(authorizationCode);
		this.currentUser = getCurrentUser().orElseThrow(UnauthorizedException::new);
		LOGGER.info("Logged in user: {}", this.currentUser.getDisplayName());

		this.queryCache = new QueryCache();
		LOGGER.info("Read {} cache entries from file", queryCache.size());
	}

	private void authorize(String authorizationCode) throws UnauthorizedException {
		AuthorizationCodeRequest authorizationCodeRequest = spotifyApi.authorizationCode(authorizationCode).build();
		try {
			AuthorizationCodeCredentials authorizationCodeCredentials = authorizationCodeRequest.execute();
			// Set access and refresh token for further "spotifyApi" object usage
			spotifyApi.setAccessToken(authorizationCodeCredentials.getAccessToken());
			spotifyApi.setRefreshToken(authorizationCodeCredentials.getRefreshToken());
		} catch (SpotifyWebApiException e) {
			if (e.getMessage().contains("Authorization")) {
				throw new UnauthorizedException("Could not get accessToken nor refreshToken", e);
			}
			throw new RuntimeException(e);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private Optional<User> getCurrentUser() {
		try {
			return Optional.ofNullable(spotifyApi.getCurrentUsersProfile().build().execute());
		} catch (IOException | SpotifyWebApiException | ParseException e) {
			LOGGER.error("Could not obtain CurrentUserProfile", e);
			return Optional.empty();
		}
	}

	public Optional<SongFromCatalog> findSong(Song song) throws IOException, SpotifyWebApiException {
		var result = queryCache.getFromCache(song);
		var cacheHit = false;
		if (result.isEmpty()) {
			result = searchSpotifyForSong(song);
			// do not cache nulls when song is not found on spotify, since I am trying to
			// improve my search query
			result.ifPresent(spotifySong -> queryCache.cache(song, spotifySong));
		} else {
			cacheHit = true;
		}

		if (LOGGER.isDebugEnabled()) {
			if (result.isEmpty()) {
				LOGGER.debug("Not found on Spotify: {}", song);
			} else {
				if (LOGGER.isTraceEnabled()) { LOGGER.trace("Found (cacheHit={}): {}", cacheHit, result.orElse(null)); }
			}
		}

		return result;
	}

	private Optional<SongFromCatalog> searchSpotifyForSong(Song song) throws IOException, SpotifyWebApiException {
		// TODO: introduce a search strategy class which allows for more compex
		// algorithms
		var queryString = makeQueryString(song);
		var searchResult = executeSearchOnSpotify(queryString);
		var hits = searchResult.getTotal();
		return hits == 0 ? Optional.empty() : selectRightResult(song, searchResult);
	}

	private Paging<Track> executeSearchOnSpotify(String queryString) throws IOException, SpotifyWebApiException {
		var searchRequest = spotifyApi.searchTracks(queryString).build();
		Paging<Track> searchResult = null;
		while (searchResult == null) {
			try {
				searchResult = searchRequest.execute();
			} catch (TooManyRequestsException e) {
				TooManyRequestsExceptionHandler.handle(LOGGER, searchRequest.getClass().getSimpleName(), e);
			} catch (ParseException e) {
				throw new RuntimeException(e);
			}
		}
		return searchResult;
	}

	private Optional<SongFromCatalog> selectRightResult(Song song, Paging<Track> searchResult) {
		// select track with highest popularity score
		Track mostPopularTrack = null;
		for (Track candidate : searchResult.getItems()) {
			if (mostPopularTrack == null || mostPopularTrack.getPopularity() < candidate.getPopularity()) {
				mostPopularTrack = candidate;
			}
		}
		return mostPopularTrack == null ? Optional.empty() : Optional.of(makeSongFromCatalog(song, mostPopularTrack));
	}

	@Override
	public CatalogPlaylist getOrCreatePlaylist(String playlistName) throws IOException, SpotifyWebApiException {
		return findPlaylistWithName(playlistName).orElseGet(() -> createPlaylistWithName(playlistName));
	}

	private Optional<CatalogPlaylist> findPlaylistWithName(String targetName)
			throws IOException, SpotifyWebApiException {
		var iterator = PlaylistIterator.create(spotifyApi);
		while (iterator.hasNext()) {
			var spotifyPlaylist = iterator.next();
			if (spotifyPlaylist.getName().equals(targetName)) {
				LOGGER.info("Found existing playlist '{}' on Spotify", spotifyPlaylist.getName());
				return Optional.of(makePlaylist(spotifyPlaylist));
			}
		}
		return Optional.empty();
	}

	private CatalogPlaylist createPlaylistWithName(String newPlaylistName) {
		var createRequest = spotifyApi.createPlaylist(currentUser.getId(), newPlaylistName).build();
		try {
			return makePlaylist(createRequest.execute());
		} catch (IOException | SpotifyWebApiException | ParseException e) {
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
		var artists = Arrays.stream(spotifyTrack.getArtists()).map(artist -> artist.getName())
				.collect(Collectors.joining(" "));
		var song = new Song(artists, spotifyTrack.getName());
		return new SongFromCatalog(song, spotifyTrack.getUri());
	}

	@Override
	public void addToPlaylist(CatalogPlaylist playlist, List<SongFromCatalog> songs)
			throws IOException, SpotifyWebApiException {
		var trackIds = songs.stream().map(song -> song.trackUri()).collect(Collectors.toList());

		// spotify accepts max. 100 songs in a single request
		int nrOfSegments = trackIds.size() / SEGMENT_SIZE + (trackIds.size() % SEGMENT_SIZE == 0 ? 0 : 1);
		int currentSegment = 1;
		var playlistId = playlist.playlistId();
		while (currentSegment <= nrOfSegments) {
			int toIndex = currentSegment * SEGMENT_SIZE;
			int fromIndex = currentSegment * SEGMENT_SIZE - SEGMENT_SIZE;
			var segment = trackIds.subList(fromIndex, Math.min(toIndex, trackIds.size()));

			addToPlaylistOnSpotify(playlistId, segment.toArray(new String[segment.size()]));
			currentSegment++;
		}
	}

	private SnapshotResult addToPlaylistOnSpotify(String playlistId, String[] trackIds)
			throws IOException, SpotifyWebApiException {
		var request = spotifyApi.addItemsToPlaylist(playlistId, trackIds).build();
		SnapshotResult response = null;
		while (response == null) {
			try {
				response = request.execute();
			} catch (TooManyRequestsException e) {
				TooManyRequestsExceptionHandler.handle(LOGGER, request.getClass().getSimpleName(), e);
			} catch (ParseException e) {
				throw new RuntimeException(e);
			}
		}
		return response;
	}

	@Override
	public Collection<SongFromCatalog> getTracksInPlaylist(CatalogPlaylist playlist)
			throws IOException, SpotifyWebApiException {
		var trackIterator = PlaylistTrackIterator.create(spotifyApi, currentUser.getId(), playlist.playlistId());
		var songsFromCatalog = new ArrayList<SongFromCatalog>(trackIterator.size());
		while (trackIterator.hasNext()) {
			var track = trackIterator.next().getTrack();
			if (track instanceof Track) {
				songsFromCatalog.add(makeSongFromCatalog((Track) track));
			} else {
				LOGGER.warn("Cannot add unsupported IPlaylistItem to playlist: " + track);
			}
		}
		return songsFromCatalog;
	}

	public void close() {
		LOGGER.info("Write out the memory cache of {} entries to file now...", queryCache.size());
		queryCache.writeCache();
	}

}
