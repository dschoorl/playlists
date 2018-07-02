package info.rsdev.playlists.services;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Comparator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.debatty.java.stringsimilarity.JaroWinkler;
import info.rsdev.playlists.domain.SongFromCatalog;
import info.rsdev.playlists.spotify.QueryStringComposer;


public class SongFromCatalogComparator implements Comparator<SongFromCatalog>, Serializable {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(SongFromCatalogComparator.class);

    private static final long serialVersionUID = 1L;
    
    public static final SongFromCatalogComparator INSTANCE = new SongFromCatalogComparator(0.99);
    
    public static final JaroWinkler similartyCalculator = new JaroWinkler();
    
    /**
     * When we fallback to compare the query strings, strings are considered equal (enough), when the calculated similarity
     * exeeds this threshold value
     */
    private final double thresholdValue;

    public SongFromCatalogComparator(double thresholdValue) {
        this.thresholdValue = thresholdValue;
    }
    
    @Override
    public int compare(SongFromCatalog thiz, SongFromCatalog that) {
        if (thiz.trackUri.equals(that.trackUri)) {
            return 0;
        }
        
        //fallback comparison: how much do both query strings have in common
        try {
            String thisQuery = QueryStringComposer.makeQueryString(thiz.song);
            String thatQuery = QueryStringComposer.makeQueryString(that.song);
            double similarity = similartyCalculator.similarity(thisQuery, thatQuery);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("%f similar [same=%b]:%n%s%n%s", similarity, similarity >= this.thresholdValue,
                        thisQuery, thatQuery));
            }
            if (similarity >= this.thresholdValue) {
                return 0;
            } else {
                return thiz.song.compareTo(that.song);
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

}
