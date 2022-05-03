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
package info.rsdev.playlists.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import info.rsdev.playlists.domain.Song;
import info.rsdev.playlists.domain.SongFromCatalog;

/**
 * This service is responsible for creating and updating a playlist hosted at a given [MusicCatalogService]
 *
 * @author Dave Schoorl
 */
@Service
public class PlaylistService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PlaylistService.class);

    private final MusicCatalogService catalogService;

    public PlaylistService(MusicCatalogService catalogService) {
        this.catalogService = catalogService;
    }

    public void fillPlaylistWithSongs(String playlistName, List<Song> songs) throws Exception {
        LOGGER.info("Searching for {} titles in playlist {}", songs.size(), playlistName);
        if (!songs.isEmpty()) {
            var playlist = catalogService.getOrCreatePlaylist(playlistName);
            var playlistTracks = keySongsByTrackUri(catalogService.getTracksInPlaylist(playlist));
            List<SongFromCatalog> songsToAddToPlaylist = new ArrayList<>(songs.size());
            var notFound = 0;
            for (Song song: songs) {
                if (isMissingInPlaylist(playlistTracks.values(), song)) {
                    var catalogSong = catalogService.findSong(song);
                    if (catalogSong.isEmpty()) {
                        notFound++;
                    }

                    boolean isAdded = catalogSong.filter(item -> !playlistTracks.containsKey(item.trackUri()))
                            .map(songsToAddToPlaylist::add)
                            .orElse(false);
                    if (LOGGER.isDebugEnabled() && isAdded) {
                        LOGGER.debug("Added to playlist: {}", catalogSong);
                    }
                }
            }

            if (notFound > 0) {
                LOGGER.warn("Found {} / of {} on spotify", songs.size() - notFound, songs.size());
            }

            catalogService.addToPlaylist(playlist, songsToAddToPlaylist);
            LOGGER.info("Added {} songs to playlist {}", songsToAddToPlaylist.size(), playlist.name());
        } else {
            LOGGER.warn("Playlist '{}' is not created on Spotify, because there are no songs to put into it" ,playlistName);
        }
    }

    private boolean isMissingInPlaylist(Collection<Song> playlistTracks, Song targetSong) {
        for (Song candidate: playlistTracks) {
            if (SongComparator.INSTANCE.compare(candidate, targetSong) == 0) {
                LOGGER.debug("Already in playlist: {} matched {}", targetSong, candidate);
                return false;
            }
        }
        return true;
    }

    private Map<String, Song> keySongsByTrackUri(Collection<SongFromCatalog> songs) {
        Map<String, Song> songsByTrackUri = new HashMap<>(songs.size());
        songs.forEach(song -> songsByTrackUri.put(song.trackUri(), song.song()));
        return songsByTrackUri;
    }
}
