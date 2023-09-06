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
package info.rsdev.playlists.spotify;

import info.rsdev.playlists.config.SpringCommonConfig;
import info.rsdev.playlists.domain.Song;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import jakarta.annotation.Resource;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertNotNull;


@Disabled("This test is meant for manual execution, because it needs a valid Spotify authorization code")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = SpringCommonConfig.class)
public class SpotifyCatalogServiceTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpotifyCatalogServiceTest.class);

    @Resource
    private SpotifyCatalogService subjectUnderTest;

    private static HashSet<Song> songsToTest = new HashSet<>();
    static {
        songsToTest.add(new Song("Bruno Mars & Cardi B", "Finesse"));
        songsToTest.add(new Song("atb", "don't stop!"));
        songsToTest.add(new Song("Jay-Z featuring Amil (Of Major Coinz) and Ja Rule", "Can I Get A ..."));
    }

    @Test
    void findSong() throws Exception {
        LOGGER.info("Start test");
        var song = new Song("Bruno Mars & Cardi B", "Finesse");
        var searchHit = subjectUnderTest.findSong(song);
        assertNotNull(searchHit);
    }}
