package info.rsdev.playlists.spotify;

import java.io.IOException;

import org.apache.hc.core5.http.ParseException;

import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.PlaylistSimplified;

public class PlaylistIterator extends BasePagingIterator<PlaylistSimplified> {
    private PlaylistIterator(SpotifyApi spotifyApi) throws IOException, SpotifyWebApiException {
        super(spotifyApi);
    }

    @Override
    public Paging<PlaylistSimplified> getResults(int offset) throws IOException, SpotifyWebApiException {
        try {
			return spotifyApi.getListOfCurrentUsersPlaylists().offset(offset).build().execute();
		} catch (ParseException | SpotifyWebApiException | IOException e) {
			throw new RuntimeException(e);
		}
    }

    //Factory method
    public static PlaylistIterator create(SpotifyApi spotifyApi) throws IOException, SpotifyWebApiException {
    	PlaylistIterator iterator = new PlaylistIterator(spotifyApi);
    	iterator.init();
        return iterator;
    }

}
