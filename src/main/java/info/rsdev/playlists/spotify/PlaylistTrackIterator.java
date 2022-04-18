package info.rsdev.playlists.spotify;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.specification.Paging;
import com.wrapper.spotify.model_objects.specification.PlaylistSimplified;
import com.wrapper.spotify.model_objects.specification.PlaylistTrack;

import java.io.IOException;

public class PlaylistTrackIterator extends BasePagingIterator<PlaylistTrack> {

    private final String userId;

    private final String playlistId;

    private PlaylistTrackIterator(SpotifyApi spotifyApi, String userId, String playlistId) throws IOException, SpotifyWebApiException {
        super(spotifyApi);
        this.userId = userId;
        this.playlistId = playlistId;
    }

    @Override
    public Paging<PlaylistTrack> getResults(int offset) throws IOException, SpotifyWebApiException {
        return spotifyApi.getPlaylistsTracks(userId, playlistId).offset(offset).build().execute();
    }

        //Factory method
        public static PlaylistTrackIterator create(SpotifyApi spotifyApi, String userId, String playlistId)
                throws IOException, SpotifyWebApiException {
            return new PlaylistTrackIterator(spotifyApi, userId, playlistId);
        }
}
