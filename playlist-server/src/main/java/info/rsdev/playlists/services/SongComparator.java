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

import info.debatty.java.stringsimilarity.JaroWinkler;
import info.rsdev.playlists.domain.Song;
import info.rsdev.playlists.spotify.QueryStringComposer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.Serializable;
import java.util.*;

public class SongComparator implements Comparator<Song>, Serializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(SongComparator.class);

    private static final long serialVersionUID = 1L;

    public static SongComparator INSTANCE = new SongComparator(0.99, 0.92);

    private static JaroWinkler similartyCalculator = new JaroWinkler();

    private final double thresholdHigh;
    private final double thresholdMid;

    /**
     * When we fall back to compare the query strings, strings are considered equal (enough), when the calculated similarity
     * exceeds this threshold value
     */
    private SongComparator(double thresholdHigh, double thresholdMid) {
        this.thresholdHigh = thresholdHigh;
        this.thresholdMid= thresholdMid;
    }

    @Override
    public int compare(Song thiz, Song that) {
        var artistSimilarity = similartyCalculator.similarity(artistOf(thiz), artistOf(that));
        var titleSimilarity = similartyCalculator.similarity(titleOf(thiz), titleOf(that));

        //if one is very high, like over 99%, the other could be less high, like 95%
        var assumedSame = artistSimilarity >= thresholdHigh && titleSimilarity >= thresholdMid || titleSimilarity >= thresholdHigh && artistSimilarity >= thresholdMid;
        if (LOGGER.isDebugEnabled() && assumedSame) {
            LOGGER.debug("artist: {}, title: {} similar [same={}]:\n{}\n{}",
                    artistSimilarity, titleSimilarity, assumedSame, thiz, that);
        }
        return assumedSame? 0 : thiz.compareTo(that);
    }

    private String titleOf(Song song) {
        return asString(QueryStringComposer.normalizeTitle(song));
    }

    private String artistOf(Song song) {
        return asString(QueryStringComposer.normalizeArtist(song));
    }

    private String asString(Set<String> words) {
        return String.join(" ", words);
    }
}
