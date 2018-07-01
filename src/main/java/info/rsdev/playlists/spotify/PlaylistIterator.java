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
import com.wrapper.spotify.model_objects.specification.PlaylistSimplified;
import com.wrapper.spotify.requests.data.playlists.GetListOfCurrentUsersPlaylistsRequest;

public class PlaylistIterator extends BasePagingIterator<PlaylistSimplified> {
	
	private PlaylistIterator(SpotifyApi spotifyApi) {
		super(spotifyApi);
	}
	
	protected Paging<PlaylistSimplified> getResults(int offset) {
		GetListOfCurrentUsersPlaylistsRequest request = getSpotifyApi().getListOfCurrentUsersPlaylists().offset(offset).build();
		try {
			return request.execute();
		} catch (SpotifyWebApiException | IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static PlaylistIterator create(SpotifyApi spotifyApi) {
	    PlaylistIterator newInstance = new PlaylistIterator(spotifyApi);
	    newInstance.initialize();
	    return newInstance;
	}
}
