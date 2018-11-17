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

import java.time.LocalDate
import java.time.temporal.ChronoField
import java.util.ArrayList

import javax.inject.Inject

import info.rsdev.playlists.domain.ChartsItem
import info.rsdev.playlists.domain.Song
import org.slf4j.LoggerFactory

import info.rsdev.playlists.dao.ChartsItemDao
import java.util.function.Consumer

/**
 * This is the default implementation of a [MusicTitleService]
 */
class MusicChartsService

@Inject
constructor(private val scrapeService: ScrapeService, private val chartsItemDao: ChartsItemDao) : MusicTitleService {

    private val LOGGER = LoggerFactory.getLogger(MusicChartsService::class.java)

    override fun init() {
        if (chartsItemDao.setupStoreWhenNeeded()) {
            LOGGER.info("New datastore created succesfully")
        }
        for (chart in MusicChart.values()) {
            val maxYear = chartsItemDao.getHighestYearStored(chart)
            if (maxYear >= 0) {
                val maxWeek = chartsItemDao.getHighestWeekStored(chart, maxYear)
                LOGGER.warn(String.format("Datastore contains data for %s from %d, week %d", chart, maxYear, maxWeek))
            }
        }
        loadData()
    }

    override fun getChartedSongsForYear(year: Short): List<Song> {
        return chartsItemDao.getReleases(year).toList()
    }

    private fun loadData() {
        val currentYear = LocalDate.now().year.toShort()
        for (chart in scrapeService.supportedCharts) {
            for (year in getFirstYearToScrape(chart)..currentYear) {
                for (weekNumber in firstWeek(chart, year.toShort())..lastWeek(currentYear, year.toShort())) {
                    val chartsItems = scrapeService.scrape(newInternetDocumentFetcher(chart, year.toShort(), weekNumber.toByte()))
                    chartsItems.forEach(Consumer<ChartsItem> { chartsItemDao.saveOrUpdate(it) })
                    LOGGER.info("writing week $weekNumber of $chart $year to persistence layer (${chartsItems.size} songs)")
                }
            }
        }
    }

    private fun getFirstYearToScrape(chart: MusicChart): Short {
        val highestYearStoredCharts = chartsItemDao.getHighestYearStored(chart)
        return if (highestYearStoredCharts >= 0) {
            highestYearStoredCharts
        } else chart.yearStarted
    }

    private fun firstWeek(chart: MusicChart, year: Short): Byte {
        val highestWeekStoredForYear = chartsItemDao.getHighestWeekStored(chart, year)
        if (highestWeekStoredForYear >= 0) {
            return highestWeekStoredForYear
        }
        return if (chart.yearStarted == year) {
            chart.weekStarted
        } else 1
    }

    private fun lastWeek(currentYear: Short, yearProcessing: Short): Byte {
        return if (currentYear == yearProcessing) {
            LocalDate.now().get(ChronoField.ALIGNED_WEEK_OF_YEAR).toByte()
        } else 53
    }

    private fun newInternetDocumentFetcher(chart: MusicChart, year: Short, weekNumber: Byte): DocumentFetcher {
        val urlTemplate = scrapeService.getUrlTemplate(chart)?:
            throw IllegalStateException("$scrapeService does not support chart $[chart]")
        return InternetChartsFetcher(String.format(urlTemplate, chart, year, weekNumber))
    }

}
