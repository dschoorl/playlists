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

import java.io.IOException;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.specification.Playlist;
import com.wrapper.spotify.model_objects.specification.PlaylistSimplified;
import com.wrapper.spotify.model_objects.specification.User;
import com.wrapper.spotify.requests.data.playlists.CreatePlaylistRequest;
import com.wrapper.spotify.requests.data.users_profile.GetCurrentUsersProfileRequest;

import info.rsdev.playlists.Playlists;
import info.rsdev.playlists.domain.ChartsPlaylist;
import info.rsdev.playlists.domain.Song;
import info.rsdev.playlists.domain.SongFromCatalog;
import info.rsdev.playlists.spotify.PlaylistIterator;

public class SpotifyCatalogService implements MusicCatalogService {
	
    private static final Logger LOGGER = LoggerFactory.getLogger(Playlists.class);

	private static final String PLAYLIST_NAME_TEMPLATE = "%d charted songs";
	
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
    }

    @Override
	public Optional<SongFromCatalog> findSong(Song song) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ChartsPlaylist getOrCreatePlaylist(short year) {
		return findPlaylistForYear(year)
				.orElseGet(() -> createPlaylistForYear(year));
	}
	
	private Optional<ChartsPlaylist> findPlaylistForYear(short year) {
		String targetName = String.format(PLAYLIST_NAME_TEMPLATE, year);
		PlaylistIterator iterator = new PlaylistIterator(spotifyApi);
		while (iterator.hasNext()) {
			PlaylistSimplified spotifyPlaylist = iterator.next();
			LOGGER.info(String.format("Encountered playlist '%s'", spotifyPlaylist.getName()));
			if (spotifyPlaylist.getName().equals(targetName)) {
				return Optional.of(makePlaylist(spotifyPlaylist));
			}
		}
		return Optional.empty();
	}
	
	private ChartsPlaylist createPlaylistForYear(short year) {
		String newPlaylistName = String.format(PLAYLIST_NAME_TEMPLATE, year);
		CreatePlaylistRequest createRequest = spotifyApi.createPlaylist(currentUser.getId(), newPlaylistName).build();
		try {
			return makePlaylist(createRequest.execute());
		} catch (SpotifyWebApiException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	private ChartsPlaylist makePlaylist(PlaylistSimplified spotifyPlaylist) {
		return new ChartsPlaylist(spotifyPlaylist.getName(), spotifyPlaylist.getId());
	}

	private ChartsPlaylist makePlaylist(Playlist spotifyPlaylist) {
		return new ChartsPlaylist(spotifyPlaylist.getName(), spotifyPlaylist.getId());
	}

	@Override
	public void addToPlaylist(SongFromCatalog song, ChartsPlaylist playlist) {
		// TODO Auto-generated method stub

	}
	
	private User getCurrentUser() {
		GetCurrentUsersProfileRequest userRequest = spotifyApi.getCurrentUsersProfile().build();
		try {
			return userRequest.execute();
		} catch (SpotifyWebApiException | IOException e) {
			throw new RuntimeException(e);
		}
	}

}
