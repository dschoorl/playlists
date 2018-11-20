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
package info.rsdev.playlists.services

import info.rsdev.playlists.domain.Song
import info.rsdev.playlists.domain.SongFromCatalog
import org.slf4j.LoggerFactory
import java.util.*
import javax.inject.Inject

/**
 * This service is responsible for creating and updating a playlist hosted at a given [MusicCatalogService]
 *
 * @author Dave Schoorl
 */
open class PlaylistService {

    private val LOGGER = LoggerFactory.getLogger(PlaylistService::class.java)

    @Inject
    private lateinit var catalogService: MusicCatalogService

    fun fillPlaylistWithSongs(playlistName: String, songs: List<Song>) {
        if (LOGGER.isInfoEnabled) {
            LOGGER.info("Searching for ${songs.size} titles in playlist $playlistName")
        }
        val playlist = catalogService.getOrCreatePlaylist(playlistName)
        val playlistTracks = keySongsByTrackUri(catalogService.getTracksInPlaylist(playlist))
        val songsToAddToPlaylist = ArrayList<SongFromCatalog>(songs.size)
        var notFound = 0
        for (song in songs) {
            if (isMissingInPlaylist(playlistTracks.values, song)) {
                val catalogSong = catalogService.findSong(song)
                if (catalogSong == null) {
                    notFound++
                }

                val isAdded = catalogSong?.takeIf { !playlistTracks.containsKey(it.trackUri) }
                                       ?.let { songsToAddToPlaylist.add(it) }
                if (LOGGER.isDebugEnabled && isAdded == true) {
                    LOGGER.debug("Added to playlist: ${catalogSong}")
                }
            }
        }

        if (notFound > 0) {
            LOGGER.warn("Found ${songs.size - notFound} / of ${songs.size} on spotify")
        }

        catalogService.addToPlaylist(playlist, songsToAddToPlaylist)
        LOGGER.info("Added ${songsToAddToPlaylist.size} songs to playlist ${playlist.name}")
    }

    private fun isMissingInPlaylist(playlistTracks: Collection<Song>, targetSong: Song): Boolean {
        for (candidate in playlistTracks) {
            if (SongComparator.INSTANCE.compare(candidate, targetSong) == 0) {
                if (LOGGER.isDebugEnabled) {
                    LOGGER.debug("Already in playlist: $targetSong matched $candidate")
                }
                return false
            }
        }
        return true
    }

    private fun keySongsByTrackUri(songs: Collection<SongFromCatalog>): Map<String, Song> {
        val songsByTrackUri = HashMap<String, Song>(songs.size)
        songs.forEach { song -> songsByTrackUri[song.trackUri] = song.song }
        return songsByTrackUri
    }

}
