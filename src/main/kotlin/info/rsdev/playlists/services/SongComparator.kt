package info.rsdev.playlists.services

import info.debatty.java.stringsimilarity.JaroWinkler
import info.rsdev.playlists.domain.Song
import info.rsdev.playlists.spotify.QueryStringComposer
import org.slf4j.LoggerFactory
import java.io.Serializable
import java.util.*


class SongComparator(
        /**
         * When we fallback to compare the query strings, strings are considered equal (enough), when the calculated similarity
         * exeeds this threshold value
         */
        private val thresholdHigh: Double, private val thresholdMid: Double) : Comparator<Song>, Serializable {

    override fun compare(thiz: Song, that: Song): Int {
        val artistSimilarity = similartyCalculator.similarity(artistOf(thiz), artistOf(that))
        val titleSimilarity = similartyCalculator.similarity(titleOf(thiz), titleOf(that))

        //if one if very high, like over 99%, the other could be less high, like 95%
        val assumedSame = artistSimilarity >= thresholdHigh && titleSimilarity >= thresholdMid || titleSimilarity >= thresholdHigh && artistSimilarity >= thresholdMid
        if (LOGGER.isDebugEnabled && assumedSame) {
            LOGGER.debug("artist: $artistSimilarity, title: $titleSimilarity similar [same=$assumedSame]:\n${thiz}\n${that}")
        }
        return if (assumedSame) {
            0
        } else {
            thiz.compareTo(that)
        }
    }

    private fun titleOf(song: Song) = asString(QueryStringComposer.normalizeTitle(song))

    private fun artistOf(song: Song) = asString(QueryStringComposer.normalizeArtist(song))

    private fun asString(words: Set<String>) = words.joinToString(" ")

    companion object {

        private val LOGGER = LoggerFactory.getLogger(SongComparator::class.java)

        private const val serialVersionUID = 1L

        val INSTANCE = SongComparator(0.99, 0.92)

        val similartyCalculator = JaroWinkler()
    }

}
