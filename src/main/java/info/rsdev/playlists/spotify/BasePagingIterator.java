package info.rsdev.playlists.spotify;

import java.io.IOException;
import java.util.Iterator;

import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.Paging;

public abstract class BasePagingIterator<T> implements Iterator<T> {
	protected final SpotifyApi spotifyApi;

	private Paging<T> results;

	private int size;

	private int pageItemsIterated = 0;
	private int totalItemsIterated = 0;

	protected BasePagingIterator(SpotifyApi spotifyApi) {
		this.spotifyApi = spotifyApi;
	}

	/**
	 * Initialize the iterator after it has been constructed, so that values needed
	 * to execute the {@link #getResults(int)} method can be processed in the parent
	 * constructor
	 * 
	 * @throws SpotifyWebApiException
	 * @throws IOException
	 */
	protected void init() throws SpotifyWebApiException, IOException {
		results = getResults();
		size = results.getTotal();
	}

	private Paging<T> getResults() throws IOException, SpotifyWebApiException {
		this.pageItemsIterated = 0;
		return getResults(totalItemsIterated);
	}

	protected abstract Paging<T> getResults(int offset) throws IOException, SpotifyWebApiException;

	@Override
	public boolean hasNext() {
		return totalItemsIterated < size;
	}

	@Override
	public T next() {
		var nextItem = results.getItems()[pageItemsIterated];
		pageItemsIterated++;
		totalItemsIterated++;

		// When we have read the last item on this page, but there are more pages, load
		// the next page now
		if (results.getItems().length == pageItemsIterated && hasNext()) {
			try {
				results = getResults();
			} catch (IOException | SpotifyWebApiException e) {
				throw new IllegalStateException(e);
			}
		}
		return nextItem;
	}

	public int size() {
		return this.size;
	}
}