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
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.rsdev.playlists.dao.ChartsItemDao;
import info.rsdev.playlists.domain.ChartsItem;

public class MusicChartsService implements SingleService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MusicChartsService.class);

    private ScrapeService scrapeService;

    private ChartsItemDao chartsItemDao;

    @Inject
    public MusicChartsService(ScrapeService scrapeService, ChartsItemDao chartsItemDao) {
        this.chartsItemDao = chartsItemDao;
        this.scrapeService = scrapeService;
    }

    @Override
    public void init() {
        if (chartsItemDao.setupStoreWhenNeeded()) {
            loadData();
        }
        
        LOGGER.info(String.format("New releaes in 2018: %d", chartsItemDao.getReleases((short)2018).size()));
    }

    private void loadData() {
    	short currentYear = (short)LocalDate.now().getYear();
        for (MusicChart chart : scrapeService.getSupportedCharts()) {
        	for (short year = chart.getYearStarted(); year <= currentYear; year++) {
	            for (byte weekNumber = firstWeek(chart, year); weekNumber <= lastWeek(currentYear, year); weekNumber++) {
	                List<ChartsItem> chartsItems = scrapeService.scrape(newInternetDocumentFetcher(chart, year, weekNumber));
	                chartsItems.forEach(chartsItemDao::saveOrUpdate);
	                LOGGER.info(String.format("writing week %d of %s %d to persistence layer (%d songs)", 
	                		weekNumber, chart, year, chartsItems.size()));
	            }
        	}
        }
    }
    
    private byte firstWeek(MusicChart chart, short year) {
    	if (chart.getYearStarted() == year) {
    		return chart.getWeekStarted();
    	}
    	return 1;
    }
    
    private byte lastWeek(short currentYear, short yearProcessing) {
    	if (currentYear == yearProcessing) {
    		return (byte)LocalDate.now().get(ChronoField.ALIGNED_WEEK_OF_YEAR);
    	}
    	return 53;
    }

    private DocumentFetcher newInternetDocumentFetcher(MusicChart chart, short year, byte weekNumber) {
    	String urlTemplate = scrapeService.getUrlTemplate(chart)
    			.orElseThrow(() -> new IllegalStateException(String.format("%s does not support chart %s", scrapeService, chart)));
        return new InternetChartsFetcher(urlTemplate, chart, year, weekNumber);
    }

}
