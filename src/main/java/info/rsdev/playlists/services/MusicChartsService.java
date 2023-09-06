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

import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import info.rsdev.playlists.dao.ChartsItemDao;
import info.rsdev.playlists.dao.Initializable;
import info.rsdev.playlists.domain.Song;
import info.rsdev.playlists.exception.FailedHostException;

/**
 * This is the default implementation of a {@link MusicTitleService}
 */
@Component
public class MusicChartsService implements MusicTitleService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MusicChartsService.class);

    private final ScrapeService scrapeService;

    private final ChartsItemDao chartsItemDao;

    public MusicChartsService(ScrapeService scrapeService, ChartsItemDao chartsItemDao) {
        this.scrapeService = scrapeService;
        this.chartsItemDao = chartsItemDao;
    }

    @Override
    public void init() {
        if (chartsItemDao instanceof Initializable init && init.setupStoreWhenNeeded()) {
            LOGGER.info("New datastore created succesfully");
        }

        for (MusicChart chart : MusicChart.values()) {
            var maxYear = chartsItemDao.getHighestYearStored(chart);
            if (maxYear >= 0) {
                var maxWeek = chartsItemDao.getHighestWeekStored(chart, maxYear);
                LOGGER.warn("Datastore contains data for {} from {}, week {}", chart, maxYear, maxWeek);
            }
        }
        loadData();
    }

    @Override
    public List<Song> getChartedSongsForYear(short year) {
        return new ArrayList<>(chartsItemDao.getReleases(year));
    }

    /**
     * Complete the database with data from the missing editions of the wanted
     * charts by retrieving them from the Internet via the {@link ScrapeService}
     */
    private void loadData() {
        short currentYear = (short) LocalDate.now().getYear();
        for (MusicChart chart : scrapeService.getSupportedCharts()) {
            try {
                for (short year = getFirstYearToScrape(chart); year <= currentYear; year++) {
                    for (byte weekNumber = firstWeek(chart, year); weekNumber <= lastWeek(currentYear, year); weekNumber++) {
                        long start = System.currentTimeMillis();
                        var chartsItems = scrapeService.scrape(newInternetDocumentFetcher(chart, year, weekNumber));
                        if (!chartsItems.isEmpty()) {
                            long scrapeMillies = System.currentTimeMillis() - start;
                            chartsItemDao.insert(chartsItems);
                            long dbMillies = System.currentTimeMillis() - start - scrapeMillies;
                            LOGGER.info("persisted week {} of {} {} ({} songs [ scrape={}ms., db={}ms.])", weekNumber, chart, year,
                                    chartsItems.size(), scrapeMillies, dbMillies);
                        }
                    }
                }
            } catch (FailedHostException e) {
                LOGGER.error("Exception during scraping", e);
                // We do not want holes in our charts, because at present we have no means to
                // detect and fix them, therefore move on with next chart.
            }
        }
    }

    private short getFirstYearToScrape(MusicChart chart) {
        var highestYearStoredCharts = chartsItemDao.getHighestYearStored(chart);
        return highestYearStoredCharts >= 0 ? highestYearStoredCharts : chart.yearStarted();
    }

    private byte firstWeek(MusicChart chart, short year) {
        var highestWeekStoredForYear = chartsItemDao.getHighestWeekStored(chart, year);
        if (highestWeekStoredForYear >= 0) {
            // We don't want to re-scrape and store the latest week already scraped
            return (byte) (highestWeekStoredForYear + 1);
        }
        return chart.yearStarted() == year ? chart.weekStarted() : 1;
    }

    private byte lastWeek(short currentYear, short yearProcessing) {
        return currentYear == yearProcessing ? (byte) LocalDate.now().get(ChronoField.ALIGNED_WEEK_OF_YEAR) : 53;
    }

    private DocumentFetcher newInternetDocumentFetcher(MusicChart chart, short year, byte weekNumber) {
        var urlTemplate = scrapeService.getUrlTemplate(chart)
                .orElseThrow(() -> new IllegalStateException(scrapeService + " does not support chart " + chart));
        return new InternetChartsFetcher(String.format(urlTemplate, chart, year, weekNumber));
    }
}
