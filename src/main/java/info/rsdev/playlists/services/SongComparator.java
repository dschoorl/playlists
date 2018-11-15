package info.rsdev.playlists.services;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;

import info.rsdev.playlists.domain.Song;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.debatty.java.stringsimilarity.JaroWinkler;
import info.rsdev.playlists.spotify.QueryStringComposer;


public class SongComparator implements Comparator<Song>, Serializable {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(SongComparator.class);

    private static final long serialVersionUID = 1L;
    
    public static final SongComparator INSTANCE = new SongComparator(0.99, 0.92);
    
    public static final JaroWinkler similartyCalculator = new JaroWinkler();
    
    /**
     * When we fallback to compare the query strings, strings are considered equal (enough), when the calculated similarity
     * exeeds this threshold value
     */
    private final double thresholdHigh;
    private final double thresholdMid;

    public SongComparator(double thresholdOne, double thresholdOther) {
        this.thresholdHigh = thresholdOne;
        this.thresholdMid = thresholdOther;
    }
    
    @Override
    public int compare(Song thiz, Song that) {
        double artistSimilarity = similartyCalculator.similarity(artistOf(thiz), artistOf(that));
        double titleSimilarity = similartyCalculator.similarity(titleOf(thiz), titleOf(that));

        //if one if very high, like over 99%, the other could be less high, like 95%
        boolean assumedSame = (artistSimilarity >= thresholdHigh && titleSimilarity >= thresholdMid) ||
                              (titleSimilarity >= thresholdHigh && artistSimilarity >= thresholdMid);
        if (LOGGER.isDebugEnabled() && assumedSame) {
            LOGGER.debug(String.format("artist: %f, title: %f similar [same=%b]:%n%s%n%s", artistSimilarity, titleSimilarity, 
                    assumedSame, thiz, that));
        }
        if (assumedSame) {
            return 0;
        } else {
            return thiz.compareTo(that);
        }
    }
    
    private String titleOf(Song song) {
        return asString(QueryStringComposer.normalizeTitle(song));
    }
    private String artistOf(Song song) {
        return asString(QueryStringComposer.normalizeArtist(song));
    }
    
    private String asString(Set<String> words) {
        return words.stream().collect(Collectors.joining(" "));
    }

}
