package info.rsdev.playlists.spotify;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.specification.Paging;
import com.wrapper.spotify.model_objects.specification.PlaylistSimplified;

import java.io.IOException;

public class PlaylistIterator extends BasePagingIterator<PlaylistSimplified> {
    private PlaylistIterator(SpotifyApi spotifyApi) throws IOException, SpotifyWebApiException {
        super(spotifyApi);
    }

    @Override
    public Paging<PlaylistSimplified> getResults(int offset) throws IOException, SpotifyWebApiException {
        return spotifyApi.getListOfCurrentUsersPlaylists().offset(offset).build().execute();
    }

    //Factory method
    public static PlaylistIterator create(SpotifyApi spotifyApi) throws IOException, SpotifyWebApiException {
        return new PlaylistIterator(spotifyApi);
    }

}
