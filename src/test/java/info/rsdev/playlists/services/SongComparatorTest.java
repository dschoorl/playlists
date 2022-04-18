package info.rsdev.playlists.services;

import info.rsdev.playlists.domain.Song;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SongComparatorTest {

    @Test
    void equalWhenArtistsAreSimilarEnough() {
        var thiz = new Song("The Scr!pt", "Arms Open");
        var that = new Song("The Script", "Arms Open");
        assertEquals(0, SongComparator.INSTANCE.compare(thiz, that));
    }

    @Test
    void equalWhenArtistsAndTitleAreSimilarEnough() {
        var thiz = new Song("New Kids", "Groeten uit Brabant");
        var that = new Song("The New Kids", "Groeten uit Brabant!");
        assertEquals(0, SongComparator.INSTANCE.compare(thiz, that));
    }
}
