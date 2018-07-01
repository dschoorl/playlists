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

import java.io.IOException;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.specification.Paging;
import com.wrapper.spotify.model_objects.specification.PlaylistTrack;
import com.wrapper.spotify.requests.data.playlists.GetPlaylistsTracksRequest;

public class PlaylistTrackIterator extends BasePagingIterator<PlaylistTrack> {
	
	private final String userId;
	
	private final String playlistId;
	
	private PlaylistTrackIterator(SpotifyApi spotifyApi, String userId, String playlistId) {
		super(spotifyApi);
		this.userId = userId;
		this.playlistId = playlistId;
	}
	
	protected Paging<PlaylistTrack> getResults(int offset) {
		GetPlaylistsTracksRequest request = getSpotifyApi().getPlaylistsTracks(userId, playlistId).offset(offset).build();
		try {
			return request.execute();
		} catch (SpotifyWebApiException | IOException e) {
			throw new RuntimeException(e);
		}
	}

    public static PlaylistTrackIterator create(SpotifyApi spotifyApi, String userId, String playlistId) {
        PlaylistTrackIterator newInstance = new PlaylistTrackIterator(spotifyApi, userId, playlistId);
        newInstance.initialize();
        return newInstance;
    }
}
