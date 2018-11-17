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
import org.jsoup.select.Collector.collect

import java.io.UnsupportedEncodingException
import java.util.Arrays
import java.util.HashMap
import java.util.HashSet
import java.util.SortedSet
import java.util.TreeSet
import java.util.stream.Collectors

object QueryStringComposer {

    private val ARTIST_NOISE_WORDS = HashSet<String>()
    private val TITLE_NOISE_WORDS = HashSet<String>()
    private val ARTIST_ALIASSES = HashMap<String, String>()

    init {
        // all entries must be lower case
        ARTIST_NOISE_WORDS.addAll(Arrays.asList("feat", "feat.", "featuring", "ft.", "ft", "the", "with", "and", "x",
                "+", "vs", "vs."))
        TITLE_NOISE_WORDS.addAll(Arrays.asList("the", "a", "de", "-", "radio", "edit", "mix", "single"))

        ARTIST_ALIASSES["atc"] = "a touch of class"
        ARTIST_ALIASSES["beegees"] = "bee gees"
    }

    fun normalizeArtist(song: Song): SortedSet<String> {
        var artistWords = splitToLowercaseWords(song.artist)
        artistWords.removeAll(ARTIST_NOISE_WORDS)
        artistWords = artistWords.map{ replaceAliasses(it) }.toSortedSet()
        return artistWords
    }

    fun normalizeTitle(song: Song): SortedSet<String> {
        val title = chooseOneWhenThereIsDoubleASide(song.title)
        val titleWords = splitToLowercaseWords(title)
        titleWords.removeAll(TITLE_NOISE_WORDS)
        return titleWords
    }

    @Throws(UnsupportedEncodingException::class)
    fun makeQueryString(song: Song): String {
        val titleWords = normalizeTitle(song)
        val artistWords = normalizeArtist(song)
        val query = StringBuilder()
        appendSearchField(query, "artist", artistWords)
        if (!artistWords.isEmpty()) {
            query.append(" ")
        }
        appendSearchField(query, "title", titleWords)
        return query.toString()
    }

    private fun chooseOneWhenThereIsDoubleASide(title: String): String {
        return if (title.contains("/")) {
            title.substring(0, title.indexOf("/"))
        } else title
    }

    private fun replaceAliasses(word: String) = ARTIST_ALIASSES[word]?:word

    @Throws(UnsupportedEncodingException::class)
    private fun appendSearchField(query: StringBuilder, fieldName: String, words: SortedSet<String>): StringBuilder {
        if (!words.isEmpty()) {
            query.append(fieldName).append(":")
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
