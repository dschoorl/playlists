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

import info.rsdev.playlists.dao.ChartsItemDao;
import info.rsdev.playlists.domain.Song;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;

/**
 * This is the default implementation of a {@link MusicTitleService}
 */
public class MusicChartsService implements MusicTitleService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MusicChartsService.class);

    private final ScrapeService scrapeService;

    private final ChartsItemDao chartsItemDao;

    @Inject
    public MusicChartsService(ScrapeService scrapeService, ChartsItemDao chartsItemDao) {
        this.scrapeService = scrapeService;
        this.chartsItemDao = chartsItemDao;
    }

    @Override
    public void init() {
        if (chartsItemDao.setupStoreWhenNeeded()) {
            LOGGER.info("New datastore created succesfully");
        }
        for (MusicChart chart: MusicChart.values()) {
            var maxYear = chartsItemDao.getHighestYearStored(chart);
            if (maxYear >= 0) {
                var maxWeek = chartsItemDao.getHighestWeekStored(chart, maxYear);
                LOGGER.warn(String.format("Datastore contains data for %s from %d, week %d", chart, maxYear, maxWeek));
            }
        }
        loadData();
    }

    @Override
    public List<Song> getChartedSongsForYear(short year) {
        return new ArrayList<>(chartsItemDao.getReleases(year));
    }

    private void loadData() {
        short currentYear = (short)LocalDate.now().getYear();
        for (MusicChart chart: scrapeService.getSupportedCharts()) {
            for (short year = getFirstYearToScrape(chart); year <=currentYear; year++) {
                for (byte weekNumber = firstWeek(chart, year); weekNumber<=lastWeek(currentYear, year); weekNumber++) {
                    var chartsItems = scrapeService.scrape(newInternetDocumentFetcher(chart, year, weekNumber));
                    chartsItems.forEach(it -> chartsItemDao.saveOrUpdate(it));
                    LOGGER.info("writing week {} of {} {} to persistence layer ({} songs)",
                            weekNumber, chart, year, chartsItems.size());
                }
            }
        }
    }

    private short getFirstYearToScrape(MusicChart chart) {
        var highestYearStoredCharts = chartsItemDao.getHighestYearStored(chart);
        return highestYearStoredCharts >= 0? highestYearStoredCharts: chart.yearStarted();
    }

    private byte firstWeek(MusicChart chart, short year) {
        var highestWeekStoredForYear = chartsItemDao.getHighestWeekStored(chart, year);
        if (highestWeekStoredForYear >= 0) {
            return highestWeekStoredForYear;
        }
        return chart.yearStarted() == year? chart.weekStarted(): 1;
    }

    private byte lastWeek(short currentYear, short yearProcessing) {
        return currentYear == yearProcessing? (byte)LocalDate.now().get(ChronoField.ALIGNED_WEEK_OF_YEAR): 53;
    }

    private DocumentFetcher newInternetDocumentFetcher(MusicChart chart, short year, byte weekNumber) {
        var urlTemplate = scrapeService.getUrlTemplate(chart)
                .orElseThrow(() -> new IllegalStateException(scrapeService + " does not support chart " + chart));
        return new InternetChartsFetcher(String.format(urlTemplate, chart, year, weekNumber));
    }
}
