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
package info.rsdev.playlists.spotify

import com.wrapper.spotify.SpotifyApi
import com.wrapper.spotify.exceptions.SpotifyWebApiException
import com.wrapper.spotify.exceptions.detailed.TooManyRequestsException
import com.wrapper.spotify.exceptions.detailed.UnauthorizedException
import com.wrapper.spotify.model_objects.special.SnapshotResult
import com.wrapper.spotify.model_objects.specification.*
import info.rsdev.playlists.domain.CatalogPlaylist
import info.rsdev.playlists.domain.Song
import info.rsdev.playlists.domain.SongFromCatalog
import info.rsdev.playlists.services.MusicCatalogService
import info.rsdev.playlists.spotify.QueryStringComposer.makeQueryString
import org.slf4j.LoggerFactory
import java.io.IOException
import java.util.*

open class SpotifyCatalogService(clientId: String, clientSecret: String, accessToken: String, refreshToken: String?) : MusicCatalogService {

    private val queryCache: QueryCache

    private val spotifyApi: SpotifyApi

    private val currentUser: User

    init {
        spotifyApi = SpotifyApi.builder()
                .setClientId(clientId)
                .setClientSecret(clientSecret)
                .setAccessToken(accessToken)
                .setRefreshToken(refreshToken)
                .build()

        val authenticatedUser: User? = getCurrentUser()
        this.currentUser = authenticatedUser?:throw UnauthorizedException()

        this.queryCache = QueryCache()
        LOGGER.info("Read ${queryCache.size()} cache entries from file")
    }

    override fun findSong(song: Song): SongFromCatalog? {
        var result = queryCache.getFromCache(song)
        var cacheHit = false
        if (result == null) {
            result = searchSpotifyForSong(song)
            //do not cache nulls when song is not found on spotify, since I am trying to improve my search query
            result?.let { spotifySong -> queryCache.cache(song, spotifySong) }
        } else {
            cacheHit = true
        }

        if (LOGGER.isDebugEnabled) {
            if (result == null) {
                LOGGER.debug("Not found on Spotify: $song")
            } else {
                if (LOGGER.isTraceEnabled) {
                    LOGGER.trace("Found (cacheHit=$cacheHit): $result")
                }
            }
        }

        return result
    }

    @Throws(IOException::class, SpotifyWebApiException::class)
    internal fun searchSpotifyForSong(song: Song): SongFromCatalog? {
        //TODO: introduce a search strategy class which allows for more compex algorithms
        val queryString = makeQueryString(song)
        val searchResult = executeSearchOnSpotify(queryString)
        val hits = searchResult.total!!
        return if (hits == 0) {
            null
        } else {
            selectRightResult(song, searchResult)
        }
    }

    @Throws(SpotifyWebApiException::class, IOException::class)
    private fun executeSearchOnSpotify(queryString: String): Paging<Track> {
        val searchRequest = spotifyApi.searchTracks(queryString).build()
        var searchResult: Paging<Track>? = null
        while (searchResult == null) {
            try {
                searchResult = searchRequest.execute()
            } catch (e: TooManyRequestsException) {
                TooManyRequestsExceptionHandler.handle(LOGGER, searchRequest.javaClass.simpleName, e)
            }

        }
        return searchResult
    }

    private fun selectRightResult(song: Song, searchResult: Paging<Track>): SongFromCatalog? {
        //select track with highest popularity score
        var mostPopularTrack: Track? = null
        for (candidate in searchResult.items) {
            if (mostPopularTrack == null || mostPopularTrack.popularity < candidate.popularity) {
                mostPopularTrack = candidate
            }
        }
        return mostPopularTrack?.let { makeSongFromCatalog(song, it) }
    }

    override fun getOrCreatePlaylist(playlistName: String): CatalogPlaylist {
        return findPlaylistWithName(playlistName)
                .orElseGet { createPlaylistWithName(playlistName) }
    }

    private fun findPlaylistWithName(targetName: String): Optional<CatalogPlaylist> {
        val iterator = PlaylistIterator.create(spotifyApi)
        while (iterator.hasNext()) {
            val spotifyPlaylist = iterator.next()
            if (spotifyPlaylist.name == targetName) {
                LOGGER.info("Found existing playlist '${spotifyPlaylist.name}' on Spotify")
                return Optional.of(makePlaylist(spotifyPlaylist))
            }
        }
        return Optional.empty()
    }

    private fun createPlaylistWithName(newPlaylistName: String): CatalogPlaylist {
        val createRequest = spotifyApi.createPlaylist(currentUser.id, newPlaylistName).build()
        return makePlaylist(createRequest.execute())
    }

    private fun makePlaylist(spotifyPlaylist: PlaylistSimplified) = CatalogPlaylist(spotifyPlaylist.name, spotifyPlaylist.id)

    private fun makePlaylist(spotifyPlaylist: Playlist) = CatalogPlaylist(spotifyPlaylist.name, spotifyPlaylist.id)

    private fun makeSongFromCatalog(song: Song, spotifyTrack: Track) = SongFromCatalog(song, spotifyTrack.uri)

    private fun makeSongFromCatalog(spotifyTrack: Track): SongFromCatalog {
        val artists = spotifyTrack.artists.joinToString(" ") { artist -> artist.getName() }
        val song = Song(artists, spotifyTrack.name)
        return SongFromCatalog(song, spotifyTrack.uri)
    }

    override fun addToPlaylist(playlist: CatalogPlaylist, songs: List<SongFromCatalog>) {
        val trackIds = songs.map { song -> song.trackUri }

        //spotify accepts max. 100 songs in a single request
        val nrOfSegments = trackIds.size / SEGMENT_SIZE + if (trackIds.size % SEGMENT_SIZE == 0) 0 else 1
        var currentSegment = 1
        val playlistId = playlist.playlistId
        while (currentSegment <= nrOfSegments) {
            val toIndex = currentSegment * SEGMENT_SIZE
            val fromIndex = currentSegment * SEGMENT_SIZE - SEGMENT_SIZE
            val segment = trackIds.subList(fromIndex, Math.min(toIndex, trackIds.size))

            addToPlaylistOnSpotify(playlistId, segment.toTypedArray())
            currentSegment++
        }
    }

    @Throws(SpotifyWebApiException::class, IOException::class)
    private fun addToPlaylistOnSpotify(playlistId: String, trackIds: Array<String>): SnapshotResult {
        val request = spotifyApi.addTracksToPlaylist(currentUser.id, playlistId, trackIds).build()
        var response: SnapshotResult? = null
        while (response == null) {
            try {
                response = request.execute()
            } catch (e: TooManyRequestsException) {
                TooManyRequestsExceptionHandler.handle(LOGGER, request.javaClass.simpleName, e)
            }
        }
        return response
    }

    private fun getCurrentUser() = spotifyApi.currentUsersProfile.build().execute()

    override fun getTracksInPlaylist(playlist: CatalogPlaylist): Collection<SongFromCatalog> {
        val trackIterator = PlaylistTrackIterator.create(spotifyApi, currentUser.id, playlist.playlistId)
        val songsFromCatalog = ArrayList<SongFromCatalog>(trackIterator.size)
        while (trackIterator.hasNext()) {
            val track = trackIterator.next().track
            songsFromCatalog.add(makeSongFromCatalog(track))
        }
        return songsFromCatalog
    }

    fun close() {
        LOGGER.info("Write out the memory cache of ${queryCache.size()} entries to file now...")
        queryCache.writeCache()
    }

    companion object {

        private val LOGGER = LoggerFactory.getLogger(SpotifyCatalogService::class.java)

        private val SEGMENT_SIZE = 50
    }

}
