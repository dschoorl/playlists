package info.rsdev.playlists.spotify;

import info.rsdev.playlists.domain.Song;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class QueryStringComposer {
    // all entries must be lower case
    private static final Set<String> CREDITS_NOISE_WORDS = Set.of("feat", "feat.", "featuring", "ft.", "ft", "mmv", "m.m.v.");
    private static final Set<String> ARTIST_NOISE_WORDS = Set.of("the", "with", "and", "x", "+", "vs", "vs.");
    private static final Set<String> TITLE_NOISE_WORDS = Set.of("the", "a", "de", "-", "radio", "edit", "mix", "single");
    private static final Map<String, String> ARTIST_ALIASSES = Map.of(
            "atc", "a touch of class",
            "beegees", "bee gees",
            "scr!pt", "script",
            "p!nk", "pink",
            "abba*teens", "a*teens");

    private static final String punctuationMarks = ",.!?'\"";

    /**
     * The fieldname in the query string that is appended before the artists keywords. As a result, the artist keywords
     * are only used to find the artist
     */
    private static final String ARTIST_FIELD = "artist";

    /**
     * The fieldname in the query string that is appended before the title keywords. As a result, the title keywords
     * are only used to find the track
     */
    private static final String TRACK_TITLE_FIELD = "track";

    public static SortedSet<String> normalizeArtist(Song song) {
        var artistWords = splitToLowercaseWords(song.artist());
        artistWords.removeAll(ARTIST_NOISE_WORDS);
        artistWords.removeAll(CREDITS_NOISE_WORDS);
        return artistWords.stream().map(part -> replaceAliasses(part)).collect(Collectors.toCollection(() -> new TreeSet<>()));
    }

    public static SortedSet<String> normalizeTitle(Song song) {
        var title = chooseOneWhenThereIsDoubleASide(song.title());
        title = stripPunctuation(title);
        var titleWords = splitToLowercaseWords(title);
        titleWords.removeAll(TITLE_NOISE_WORDS);
        titleWords.removeAll(CREDITS_NOISE_WORDS);

        //Remove artist words from title
        var artistWords =  normalizeArtist(song);
        titleWords.removeAll(artistWords);
        return titleWords;
    }

    private static String stripPunctuation(String text) {
        var sb = new StringBuilder(text.length());
        for (var i=0; i<text.length(); i++) {
            if (punctuationMarks.indexOf(text.charAt(i))<0) {
                sb.append(text.charAt(i));
            }
        }
        return sb.toString();
    }

    public static String makeQueryString(Song song) throws UnsupportedEncodingException {
        var titleWords = normalizeTitle(song);
        var artistWords = normalizeArtist(song);
        var query = new StringBuilder();
        appendSearchField(query, null, artistWords);
        if (!artistWords.isEmpty()) {
            query.append(" ");
        }
        appendSearchField(query, TRACK_TITLE_FIELD, titleWords);
        return query.toString();
    }

    private static String chooseOneWhenThereIsDoubleASide(String title) {
        return (title.contains("/"))? title.substring(0, title.indexOf("/")): title;
    }

    private static String replaceAliasses(String word) {
        return ARTIST_ALIASSES.getOrDefault(word, word);
    }

    private static StringBuilder appendSearchField(StringBuilder query, String fieldName, SortedSet<String>words)  throws UnsupportedEncodingException {
        if (!words.isEmpty()) {
            if (fieldName != null) {
                query.append(fieldName).append(":");
            }
            query.append(String.join(" ", words));
        }
        return query;
    }

    private static SortedSet<String> splitToLowercaseWords(String field) {
        var words = field.toLowerCase().split("[\\s\\(\\)\\,\\&]");
        SortedSet<String> result = new TreeSet<>(); // apply alphabetic ordering to make unit testing easier
        for (var word: words) {
            if (!word.isEmpty()) {
                result.add(word);
            }
        }
        return result;
    }

}
