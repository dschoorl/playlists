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
package info.rsdev.playlists.services

import info.rsdev.playlists.testutils.TestFileDocumentFetcher
import org.junit.Before
import org.junit.Test
import java.nio.file.Paths
import kotlin.test.assertEquals

class Top40ScrapeServiceTest {

    private val top40Document = Paths.get("src", "test", "resources", "data", "top40-example.html")
    private val tipparadeDocument = Paths.get("src", "test", "resources", "data", "tipparade-example.html")

    private lateinit var serviceUnderTest: Top40ScrapeService

    private val top40DocumentFetcher = TestFileDocumentFetcher(top40Document)
    private val tipparadeDocumentFetcher = TestFileDocumentFetcher(tipparadeDocument)

    @Before
    fun setup() {
        serviceUnderTest = Top40ScrapeService()
    }

    @Test
    fun scrapeTop40() {
        val hits = serviceUnderTest.scrape(top40DocumentFetcher)
        assertEquals(40, hits.size.toLong())
    }

    @Test
    fun scrapeTipparade() {
        val hits = serviceUnderTest.scrape(tipparadeDocumentFetcher)
        assertEquals(30, hits.size.toLong())
    }

    @Test
    fun getTop40Chartname() {
        assertEquals("Top 40", serviceUnderTest.getChartName(top40DocumentFetcher.fetch().get()))
    }

    @Test
    fun getTipparadeChartname() {
        assertEquals("Tipparade", serviceUnderTest.getChartName(tipparadeDocumentFetcher.fetch().get()))
    }

    @Test
    fun getYearFromTitle() {
        assertEquals(2018, serviceUnderTest.getYearOfChart(tipparadeDocumentFetcher.fetch().get()).toLong())
    }

    @Test
    fun getSingleDigitWeeknumberFromTitle() {
        assertEquals(1, serviceUnderTest.getWeekOfChart(tipparadeDocumentFetcher.fetch().get()).toLong())
    }

    @Test
    fun getTwoDigitWeeknumberFromTitle() {
        assertEquals(11, serviceUnderTest.getWeekOfChart(top40DocumentFetcher.fetch().get()).toLong())
    }
}
