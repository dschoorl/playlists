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
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.time.LocalDate
import java.util.*

import javax.inject.Inject

/**
 * @author Dave Schoorl
 */
@SpringBootApplication
class Playlists : CommandLineRunner {

    @Inject
    private lateinit var titleService: MusicTitleService

    @Inject
    private lateinit var playlistService: PlaylistService

    override fun run(args: Array<String>) {
        val startTime = System.currentTimeMillis()

        LOGGER.info("Program arguments: ${Arrays.toString(args)}")
        val year: Short = getYear(args)

        titleService.init()
        LOGGER.info(String.format("Datastore initialized after %ds", (System.currentTimeMillis() - startTime) / 1000))

        val chartedSongs = titleService.getChartedSongsForYear(year)
        playlistService.fillPlaylistWithSongs(String.format(PLAYLIST_NAME_TEMPLATE, year), chartedSongs)

        LOGGER.info(String.format("Finished: %ds", (System.currentTimeMillis() - startTime) / 1000))
    }

    fun getYear(args: Array<String>) : Short {
        //too little time, implemented quick and dirty -- no validation or error checking
        return args[0]?.toShort()?:LocalDate.now().year.toShort()
    }

    companion object {

        private val PLAYLIST_NAME_TEMPLATE = "%d charted songs"

        private val LOGGER = LoggerFactory.getLogger(Playlists::class.java)

        /**
         * @param args the command line arguments
         */
        @JvmStatic
        fun main(args: Array<String>) {
            runApplication<Playlists>(*args)
        }
    }

}
