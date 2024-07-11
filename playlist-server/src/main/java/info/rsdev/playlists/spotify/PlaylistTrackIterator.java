package info.rsdev.playlists.spotify;

import java.io.IOException;
import java.util.Objects;

import org.apache.hc.core5.http.ParseException;

import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;

public class PlaylistTrackIterator extends BasePagingIterator<PlaylistTrack> {

	private final String playlistId;

	private PlaylistTrackIterator(SpotifyApi spotifyApi, String playlistId)
			throws IOException, SpotifyWebApiException {
		super(spotifyApi);
		this.playlistId = Objects.requireNonNull(playlistId);
	}

	@Override
	public Paging<PlaylistTrack> getResults(int offset) throws IOException, SpotifyWebApiException {
		try {
			return spotifyApi.getPlaylistsItems(playlistId).offset(offset).build().execute();
		} catch (ParseException | SpotifyWebApiException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	// Factory method
	public static PlaylistTrackIterator create(SpotifyApi spotifyApi, String playlistId)
			throws IOException, SpotifyWebApiException {
		PlaylistTrackIterator iterator = new PlaylistTrackIterator(spotifyApi, playlistId);
		iterator.init();
		return iterator;
	}
}
