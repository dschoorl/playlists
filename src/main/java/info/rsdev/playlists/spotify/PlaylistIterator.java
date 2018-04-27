package info.rsdev.playlists.spotify;

import java.io.IOException;
import java.util.Iterator;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.specification.Paging;
import com.wrapper.spotify.model_objects.specification.PlaylistSimplified;
import com.wrapper.spotify.requests.data.playlists.GetListOfCurrentUsersPlaylistsRequest;

public class PlaylistIterator implements Iterator<PlaylistSimplified> {
	
	private final SpotifyApi spotifyApi;
	private Paging<PlaylistSimplified> results = null;
	private final int totalItems;
	private int pageItemsIterated;
	private int totalItemsIterated = 0;
	
	public PlaylistIterator(SpotifyApi spotifyApi) {
		this.spotifyApi = spotifyApi;
		results = getResults(0);
		totalItems = results.getTotal();
	}
	
	private Paging<PlaylistSimplified> getResults(int offset) {
		GetListOfCurrentUsersPlaylistsRequest request = spotifyApi.getListOfCurrentUsersPlaylists().offset(offset).build();
		try {
			this.pageItemsIterated = 0;
			return request.execute();
		} catch (SpotifyWebApiException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean hasNext() {
		return totalItemsIterated < totalItems;
	}

	@Override
	public PlaylistSimplified next() {
		PlaylistSimplified nextItem = results.getItems()[pageItemsIterated];
		pageItemsIterated++;
		totalItemsIterated++;
		
		//When we have read the last item on this page, but there are more pages, load the next page now
		if ((results.getItems().length == pageItemsIterated) && hasNext()) {
			results = getResults(totalItemsIterated);
		}
		return nextItem;
	}

}