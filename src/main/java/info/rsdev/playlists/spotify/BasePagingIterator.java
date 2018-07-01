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

import java.util.Iterator;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.model_objects.specification.Paging;

/**
 *
 * @param <T> the object type to iterate over
 */
public abstract class BasePagingIterator<T> implements Iterator<T> {
	
	private final SpotifyApi spotifyApi;
	private Paging<T> results = null;
	private int totalItems;
	private int pageItemsIterated;
	private int totalItemsIterated = 0;
	
	public BasePagingIterator(SpotifyApi spotifyApi) {
		this.spotifyApi = spotifyApi;
	}
	
	protected void initialize() {
        results = getResults();
        totalItems = results.getTotal();
	}
	
	private Paging<T> getResults() {
		this.pageItemsIterated = 0;
		return getResults(totalItemsIterated);
	}
	
	protected abstract Paging<T> getResults(int offset);
	
	protected SpotifyApi getSpotifyApi() {
		return this.spotifyApi;
	}
	
	@Override
	public boolean hasNext() {
		return totalItemsIterated < totalItems;
	}

	@Override
	public T next() {
		T nextItem = results.getItems()[pageItemsIterated];
		pageItemsIterated++;
		totalItemsIterated++;
		
		//When we have read the last item on this page, but there are more pages, load the next page now
		if ((results.getItems().length == pageItemsIterated) && hasNext()) {
			results = getResults();
		}
		return nextItem;
	}
	
	public int getSize() {
		return this.totalItems;
	}

}
