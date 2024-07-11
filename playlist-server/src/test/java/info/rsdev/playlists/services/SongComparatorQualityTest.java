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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import info.rsdev.playlists.domain.Song;

/**
 * Run a number of comparisons that did not match as equals with earlier implementations of
 */
@RunWith(Parameterized.class)
@Ignore("Enable when you improve the algorithm of the SongComparator-class")
public class SongComparatorQualityTest {

    private Song thizSong;
    private Song thatSong;
    private boolean shouldBeSame;

    public SongComparatorQualityTest(Song thizSong, Song thatSong, boolean shouldBeSame) {
        this.thatSong = thatSong;
        this.thizSong = thizSong;
        this.shouldBeSame = shouldBeSame;
    }

    @Parameterized.Parameters
    public static Collection<List<Object>> getProblemSongs() {
        return List.of(
                Arrays.asList(new Song("Alle Farben, Graham Candy", "She Moves"), new Song("Alle Farben, Graham Candy", "She Moves (Far Away) - Club Mix"), true),
                Arrays.asList(new Song("Felix Jaehn feat. Marc E. Bassy & Gucci Mane", "Cool"), new Song("Felix Jaehn, Marc E. Bassy, Gucci Mane", "Cool (feat. Marc E. Bassy, Gucci Mane)"), true)
        );
    }

    @Test
    public void compareSongs() {
        if (shouldBeSame) {
            assertEquals(0, SongComparator.INSTANCE.compare(thizSong, thatSong));
        } else {
            assertNotEquals(0, SongComparator.INSTANCE.compare(thizSong, thatSong));
        }
    }}
