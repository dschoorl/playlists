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


import info.rsdev.playlists.domain.Song
import org.junit.Assert.*
import org.junit.Test

class QueryStringComposerTest {

    @Test
    fun omit_The_ArticleFromArtistAndTitleKeywords() {
        val queryString = QueryStringComposer.makeQueryString(Song("the band", "the song"))
        assertEquals("artist:band title:song", queryString)
    }

    @Test
    fun omit_A_ArticleFromTitleKeywords() {
        val queryString = QueryStringComposer.makeQueryString(Song("a singer", "a song"))
        assertEquals("artist:a singer title:song", queryString)
    }

    @Test
    fun orderKeyWordsArtistAndTitleAlphabetically() {
        //this is done to make test asserts easier
        val queryString = QueryStringComposer.makeQueryString(Song("zero downtime", "three two one"))
        assertEquals("artist:downtime zero title:one three two", queryString)
    }
}
