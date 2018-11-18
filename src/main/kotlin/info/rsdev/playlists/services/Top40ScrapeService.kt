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

import info.rsdev.playlists.domain.ChartsItem
import info.rsdev.playlists.domain.Song
import info.rsdev.playlists.services.MusicChart.TIPPARADE
import info.rsdev.playlists.services.MusicChart.TOP40
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.slf4j.LoggerFactory
import java.util.*
import java.util.regex.Pattern

open class Top40ScrapeService : ScrapeService {

    private val SUPPORTED_CHARTS = listOf(TOP40, TIPPARADE)

    override val supportedCharts: List<MusicChart>
        get() = SUPPORTED_CHARTS

    override fun scrape(fetcher: DocumentFetcher) = fetcher.fetch()?.let { getChartsItems(it) }?:emptyList()

    private fun getChartsItems(top40Page: Document): List<ChartsItem> {
        val listItems = top40Page.select("div.listItem")
        val chartItems = ArrayList<ChartsItem>(listItems.size)

        val chartName = getChartName(top40Page)
        val year = getYearOfChart(top40Page)
        val weekNumber = getWeekOfChart(top40Page)

        //start with index 1, because we need to skip the header row
        for (i in 1 until listItems.size) {
            if (isChartEntry(listItems[i])) {
                try {
                    chartItems.add(getChartsItem(listItems[i], chartName, year, weekNumber))
                } catch (e: RuntimeException) {
                    LOGGER.error("Error context [$i]:\n${listItems[i]}")
                }
            }
        }
        if (LOGGER.isInfoEnabled) {
            LOGGER.info("Scraped week $weekNumber of $chartName $year")
        }
        if (LOGGER.isDebugEnabled) {
            chartItems.forEach { item -> LOGGER.debug(item.toString()) }
        }
        return chartItems
    }

    private fun isChartEntry(itemElement: Element) = itemElement.selectFirst("div.dot-icon").text() != "-"

    internal fun getWeekOfChart(top40Page: Document): Byte {
        val title = top40Page.title()
        val matcher = WEEKNUMBER_PATTERN.matcher(title)
        if (matcher.find()) {
            return java.lang.Byte.parseByte(matcher.group(1))
        }
        throw IllegalStateException("Weeknumber of chart not found in title: $title")
    }

    internal fun getYearOfChart(top40Page: Document) =
            //the page title ends with the 4 digit year
            top40Page.title().let { it.substring(it.length - 4) }.toShort()

    internal fun getChartName(top40Page: Document): String {
        val chartNameElement = top40Page.selectFirst("ul.hitlist li.active h1")
        return chartNameElement.text()
    }

    private fun getChartsItem(itemElement: Element, chartName: String, year: Short, weekNumber: Byte): ChartsItem {
        val songTitle = itemElement.selectFirst("div.song-details h3.title").text()
        val artist = itemElement.selectFirst("div.song-details p.artist").text()
        val position = java.lang.Byte.parseByte(itemElement.selectFirst("div.dot-icon").text())
        val isNewInChart = isNewInChart(itemElement)
        return ChartsItem(chartName, year, weekNumber, position, isNewInChart, Song(artist, songTitle))
    }

    internal fun isNewInChart(itemElement: Element): Boolean {
        val statColumns = itemElement.select("div.statcolumn strong")
        return if (statColumns.size >= 2) {
            statColumns[1].text() == "1"
        } else false
    }

    override fun getUrlTemplate(chart: MusicChart): String? {
        return if (SUPPORTED_CHARTS.contains(chart)) {
            URL_TEMPLATE
        } else null
    }

    companion object {

        private val LOGGER = LoggerFactory.getLogger(Top40ScrapeService::class.java)

        /* The first parameter is the year and the second the weeknumber. The earliest edition of the top40 is 1965, week 1.
     * Weeknumbers could range from 1 to 53. Sometimes, the year starts with week 2.
     */
        private val URL_TEMPLATE = "https://www.top40.nl/%s/%d/week-%d"

        private val WEEKNUMBER_PATTERN = Pattern.compile(".*week (\\d\\d?), .*")

    }

}
