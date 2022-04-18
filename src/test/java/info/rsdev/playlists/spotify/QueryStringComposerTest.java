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
package info.rsdev.playlists.spotify;

import info.rsdev.playlists.domain.Song;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class QueryStringComposerTest {
    @Test
    public void omit_The_ArticleFromArtistAndTitleKeywords() throws Exception {
        var queryString = QueryStringComposer.makeQueryString(new Song("the band", "the song"));
        assertEquals("band track:song", queryString);
    }

    @Test
    public void omit_A_ArticleFromTitleKeywords() throws Exception {
        var queryString = QueryStringComposer.makeQueryString(new Song("a singer", "a song"));
        assertEquals("a singer track:song", queryString);
    }

    @Test
    public void stripPuctuationFromTitle() throws Exception {
        var queryString = QueryStringComposer.makeQueryString(new Song("abt", "don't stop"));
        assertEquals("abt track:dont stop", queryString);
    }

    @Test
    public void resolveArtistAliasses() throws Exception {
        var queryString = QueryStringComposer.makeQueryString(new Song("ABBA*Teens", "Mamma Mia"));
        assertEquals("a*teens track:mamma mia", queryString);
    }

    @Test
    public void orderKeyWordsArtistAndTitleAlphabetically() throws Exception {
        //this is done to make test asserts easier
        var queryString = QueryStringComposer.makeQueryString(new Song("zero downtime", "three two one"));
        assertEquals("downtime zero track:one three two", queryString);
    }
}
