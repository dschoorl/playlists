package info.rsdev.playlists.services;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import info.rsdev.playlists.domain.Song;


public class SongComparatorTest {

    @Test
    public void equalWhenArtistsAreSimilarEnough() {
        Song thiz = new Song("The Scr!pt", "Arms Open");
        Song that = new Song("The Script", "Arms Open");
        assertEquals(0, SongComparator.INSTANCE.compare(thiz, that));
    }
    
    @Test
    public void equalWhenArtistsAndTitleAreSimilarEnough() {
        Song thiz = new Song("New Kids", "Groeten uit Brabant");
        Song that = new Song("The New Kids", "Groeten uit Brabant!");
        assertEquals(0, SongComparator.INSTANCE.compare(thiz, that));
    }
    
}
