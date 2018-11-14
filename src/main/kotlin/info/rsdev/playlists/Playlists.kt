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
package info.rsdev.playlists

import info.rsdev.playlists.services.MusicTitleService
import info.rsdev.playlists.services.PlaylistService
import org.slf4j.LoggerFactory
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

import javax.inject.Inject

/**
 * @author Dave Schoorl
 */
@SpringBootApplication
class Playlists {

    @Inject
    private val titleService: MusicTitleService? = null

    @Inject
    private val playlistService: PlaylistService? = null

    private fun start(args: Array<String>) {
        val startTime = System.currentTimeMillis()
        titleService!!.init()
        LOGGER.info(String.format("Datastore initialized after %ds", (System.currentTimeMillis() - startTime) / 1000))

        //Change the value of 'year' below to select the year for which you want to create a playlist
        val year: Short = 2018

        val chartedSongs = titleService.getChartedSongsForYear(year)
        playlistService!!.fillPlaylistWithSongs(String.format(PLAYLIST_NAME_TEMPLATE, year), chartedSongs)

        LOGGER.info(String.format("Finished: %ds", (System.currentTimeMillis() - startTime) / 1000))
    }

    companion object {

        private val PLAYLIST_NAME_TEMPLATE = "%d charted songs"

        private val LOGGER = LoggerFactory.getLogger(Playlists::class.java)

        /**
         * @param args the command line arguments
         */
        @JvmStatic
        fun main(args: Array<String>) {
            val iocContext = SpringApplication.run(Playlists::class.java, *args)
            val playlists = iocContext.getBean(Playlists::class.java)
            playlists.start(args)
        }
    }

}
