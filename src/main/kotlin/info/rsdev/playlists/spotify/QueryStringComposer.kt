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

import java.io.UnsupportedEncodingException
import java.util.SortedSet
import java.util.TreeSet

object QueryStringComposer {

    // all entries must be lower case
    private val CREDITS_NOISE_WORDS = hashSetOf("feat", "feat.", "featuring", "ft.", "ft")
    private val ARTIST_NOISE_WORDS = hashSetOf("the", "with", "and", "x", "+", "vs", "vs.")
    private val TITLE_NOISE_WORDS = hashSetOf("the", "a", "de", "-", "radio", "edit", "mix", "single")
    private val ARTIST_ALIASSES = hashMapOf("atc" to "a touch of class",
            "beegees" to "bee gees", "scr!pt" to "script")

    /**
     * The fieldname in the query string that is appended before the artists keywords. As a result, the artist keywords
     * are only used to find the artist
     */
    private val ARTIST_FIELD = "artist"

    /**
     * The fieldname in the query string that is appended before the title keywords. As a result, the title keywords
     * are only used to find the track
     */
    private val TRACK_TITLE_FIELD = "track"

    fun normalizeArtist(song: Song): SortedSet<String> {
        var artistWords = splitToLowercaseWords(song.artist)
        artistWords.removeAll(ARTIST_NOISE_WORDS)
        artistWords.removeAll(CREDITS_NOISE_WORDS)
        artistWords = artistWords.map{ replaceAliasses(it) }.toSortedSet()
        return artistWords
    }

    fun normalizeTitle(song: Song): SortedSet<String> {
        val title = chooseOneWhenThereIsDoubleASide(song.title)
        val titleWords = splitToLowercaseWords(title)
        titleWords.removeAll(TITLE_NOISE_WORDS)
        titleWords.removeAll(CREDITS_NOISE_WORDS)

        //Remove artist words from title
        val artistWords =  normalizeArtist(song)
        titleWords.removeAll(artistWords)

        //TODO: strip punctuation marks

        return titleWords
    }

    @Throws(UnsupportedEncodingException::class)
    fun makeQueryString(song: Song): String {
        val titleWords = normalizeTitle(song)
        val artistWords = normalizeArtist(song)
        val query = StringBuilder()
        appendSearchField(query, null, artistWords)
        if (!artistWords.isEmpty()) {
            query.append(" ")
        }
        appendSearchField(query, TRACK_TITLE_FIELD, titleWords)
        return query.toString()
    }

    private fun chooseOneWhenThereIsDoubleASide(title: String): String {
        return if (title.contains("/")) {
            title.substring(0, title.indexOf("/"))
        } else title
    }

    private fun replaceAliasses(word: String) = ARTIST_ALIASSES[word]?:word

    @Throws(UnsupportedEncodingException::class)
    private fun appendSearchField(query: StringBuilder, fieldName: String?, words: SortedSet<String>): StringBuilder {
        if (!words.isEmpty()) {
            if (fieldName != null) {
                query.append(fieldName).append(":")
            }
            query.append(words.joinToString(" "))
        }
        return query
    }

    private fun splitToLowercaseWords(field: String): SortedSet<String> {
        val words = field.toLowerCase().split("[\\s\\(\\)\\,\\&]".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val result = TreeSet<String>() // apply alphabetic ordering to make unit testing easier
        for (word in words) {
            if (!word.isEmpty()) {
                result.add(word)
            }
        }
        return result
    }

}
