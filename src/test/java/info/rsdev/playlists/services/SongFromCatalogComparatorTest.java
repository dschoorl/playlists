package info.rsdev.playlists.services;

import static org.junit.Assert.assertEquals;

import org.junit.Ignore;
import org.junit.Test;

import info.rsdev.playlists.domain.Song;
import info.rsdev.playlists.domain.SongFromCatalog;


public class SongFromCatalogComparatorTest {

    @Test
    public void equalWhenTrackUriAreEqual() {
        SongFromCatalog thiz = new SongFromCatalog(new Song("The Scr!pt", "ArmsOpen"), "uri://1121");
        SongFromCatalog that = new SongFromCatalog(new Song("The Script", "Arms Open"), "uri://1121");
        assertEquals(0, SongFromCatalogComparator.INSTANCE.compare(thiz, that));
    }

    @Test @Ignore("How to fix this")
    public void equalWhenArtistsAreSimilarEnough() {
        SongFromCatalog thiz = new SongFromCatalog(new Song("The Scr!pt", "Arms Open"), "uri://123");
        SongFromCatalog that = new SongFromCatalog(new Song("The Script", "Arms Open"), "uri://abc");
        assertEquals(0, SongFromCatalogComparator.INSTANCE.compare(thiz, that));
    }
    
    @Test
    public void equalWhenArtistsAndTitleAreSimilarEnough() {
        SongFromCatalog thiz = new SongFromCatalog(new Song("New Kids", "Groeten uit Brabant"), "uri://123");
        SongFromCatalog that = new SongFromCatalog(new Song("The New Kids", "Groeten uit Brabant!"), "uri://abc");
        assertEquals(0, SongFromCatalogComparator.INSTANCE.compare(thiz, that));
    }
    
}
