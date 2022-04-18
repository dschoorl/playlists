package info.rsdev.playlists.spotify;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.specification.Paging;

import java.io.IOException;
import java.util.Iterator;

public abstract class BasePagingIterator<T> implements Iterator<T> {
    protected final SpotifyApi spotifyApi;

    private Paging<T> results;

    private final int size;

    private int pageItemsIterated = 0;
    private int totalItemsIterated = 0;

    protected BasePagingIterator(SpotifyApi spotifyApi) throws IOException, SpotifyWebApiException {
        this.spotifyApi = spotifyApi;
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
    public T next()  {
        var nextItem = results.getItems()[pageItemsIterated];
        pageItemsIterated++;
        totalItemsIterated++;

        //When we have read the last item on this page, but there are more pages, load the next page now
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