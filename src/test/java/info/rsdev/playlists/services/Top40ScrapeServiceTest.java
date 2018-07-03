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

import info.rsdev.playlists.domain.ChartsItem;
import info.rsdev.playlists.testutils.TestfileDocumentFetcher;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Paths;
import java.util.List;

import static org.junit.Assert.*;

public class Top40ScrapeServiceTest {

    private Top40ScrapeService serviceUnderTest = null;

    private DocumentFetcher documentFetcher = null;

    @Before
    public void setup() {
        serviceUnderTest = new Top40ScrapeService();
        documentFetcher = new TestfileDocumentFetcher(Paths.get("src", "test", "resources", "data", "top40-example.html"));
    }

    @Test
    public void scrapeTop40() {
        List<ChartsItem> hits = serviceUnderTest.scrape(documentFetcher);
        assertEquals(40, hits.size());
    }

    @Test
    public void scrapeTipparade() {
        DocumentFetcher documentFetcher = new TestfileDocumentFetcher(Paths.get("src", "test", "resources", "data", "tipparade-example.html"));
        List<ChartsItem> hits = serviceUnderTest.scrape(documentFetcher);
        assertEquals(30, hits.size());
    }

    @Test
    public void getTop40Chartname() {
        assertEquals("Top 40", serviceUnderTest.getChartName(documentFetcher.fetch().get()));
    }

    @Test
    public void getTipparadeChartname() {
        DocumentFetcher documentFetcher = new TestfileDocumentFetcher(Paths.get("src", "test", "resources", "data", "tipparade-example.html"));
        assertEquals("Tipparade", serviceUnderTest.getChartName(documentFetcher.fetch().get()));
    }

    @Test
    public void getYearFromTitle() {
        DocumentFetcher documentFetcher = new TestfileDocumentFetcher(Paths.get("src", "test", "resources", "data", "tipparade-example.html"));
        assertEquals(2018, serviceUnderTest.getYearOfChart(documentFetcher.fetch().get()));
    }

    @Test
    public void getSingleDigitWeeknumberFromTitle() {
        DocumentFetcher documentFetcher = new TestfileDocumentFetcher(Paths.get("src", "test", "resources", "data", "tipparade-example.html"));
        assertEquals(1, serviceUnderTest.getWeekOfChart(documentFetcher.fetch().get()));
    }

    @Test
    public void getTwoDigitWeeknumberFromTitle() {
        assertEquals(11, serviceUnderTest.getWeekOfChart(documentFetcher.fetch().get()));
    }
}
