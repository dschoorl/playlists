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

import info.rsdev.playlists.testutils.TestFileDocumentFetcher;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Top40ScrapeServiceTest {

    private Path top40Document = Paths.get("src", "test", "resources", "data", "top40-example.html");
    private Path tipparadeDocument = Paths.get("src", "test", "resources", "data", "tipparade-example.html");

    private Top40ScrapeService serviceUnderTest;

    private TestFileDocumentFetcher top40DocumentFetcher = new TestFileDocumentFetcher(top40Document);
    private TestFileDocumentFetcher tipparadeDocumentFetcher = new TestFileDocumentFetcher(tipparadeDocument);

    @Before
    public void setup() {
        serviceUnderTest = new Top40ScrapeService();
    }

    @Test
    public void scrapeTop40() {
        var hits = serviceUnderTest.scrape(top40DocumentFetcher);
        assertEquals(40, hits.size());
    }
    
    @Test
    public void scrapeTipparade() {
        var hits = serviceUnderTest.scrape(tipparadeDocumentFetcher);
        assertEquals(30, hits.size());
    }
}
