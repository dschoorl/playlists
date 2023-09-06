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
package info.rsdev.playlists;

import java.time.LocalDate;
import java.util.Arrays;

import jakarta.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import info.rsdev.playlists.services.MusicTitleService;
import info.rsdev.playlists.services.PlaylistService;

/**
 * @author Dave Schoorl
 */
@SpringBootApplication
public class Playlists implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(Playlists.class);
    private static final String PLAYLIST_NAME_TEMPLATE = "%d charted songs";
    
    @Resource
    private MusicTitleService titleService;

    @Resource
    private PlaylistService playlistService;

    public static void main(String[] args) {
        SpringApplication.run(Playlists.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        var startTime = System.currentTimeMillis();

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Program arguments: {}", Arrays.toString(args));
        }
        var year = getYear(args);

        titleService.init();
        LOGGER.info("Datastore initialized after {}s", (System.currentTimeMillis() - startTime) / 1000);

        var chartedSongs = titleService.getChartedSongsForYear(year);
        playlistService.fillPlaylistWithSongs(String.format(PLAYLIST_NAME_TEMPLATE, year), chartedSongs);

        LOGGER.info("Finished: {}s", (System.currentTimeMillis() - startTime) / 1000);
    }

    private short getYear(String[] args) {
        //too little time, implemented quick and dirty -- no validation or error checking
        return args.length == 1 ? Short.parseShort(args[0]) : (short)LocalDate.now().getYear();
    }
}
