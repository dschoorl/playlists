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
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

/**
 * Run a number of comparisons that did not match as equals with earlier implementations of
 */
@RunWith(Parameterized::class)
@Ignore("Enable when you improve the algorithm of the SongComparator-class")
class SongComparatorQualityTest(private val thizSong: Song, private val thatSong: Song, private val shouldBeSame: Boolean) {

    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun getProblemSongs(): Collection<Array<Any>> {
            return listOf(
                    arrayOf(Song("Alle Farben, Graham Candy", "She Moves"), Song("Alle Farben, Graham Candy", "She Moves (Far Away) - Club Mix"), true)
            )
        }
    }

    @Test
    fun compareSongs() {
        if (shouldBeSame) {
            assertEquals(0, SongComparator.INSTANCE.compare(thizSong, thatSong).toLong())
        } else {
            assertNotEquals(0, SongComparator.INSTANCE.compare(thizSong, thatSong).toLong())
        }
    }
}
