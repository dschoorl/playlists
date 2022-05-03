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
import info.rsdev.playlists.domain.Song;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static info.rsdev.playlists.services.MusicChart.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class Top40ScrapeService implements ScrapeService {

    private static final List<MusicChart> SUPPORTED_CHARTS = List.of(TOP40, TIPPARADE);
    private static final Logger LOGGER = LoggerFactory.getLogger(Top40ScrapeService.class);

    /* The first parameter is the year and the second the weeknumber. The earliest edition of the top40 is 1965, week 1.
     * Weeknumbers could range from 1 to 53. Sometimes, the year starts with week 2.
     */
    private static final String URL_TEMPLATE = "https://www.top40.nl/%s/%d/week-%d";

    private static final Pattern WEEKNUMBER_PATTERN = Pattern.compile(".*week (\\d\\d?), .*");

    @Override
    public List<MusicChart> getSupportedCharts() {
        return Collections.unmodifiableList(SUPPORTED_CHARTS);
    }

    @Override
    public List<ChartsItem> scrape(DocumentFetcher fetcher) {
        return fetcher.fetch().map(this::getChartsItems).orElse(Collections.emptyList());
    }

    private List<ChartsItem> getChartsItems(Document top40Page) {
        var listItems = top40Page.select("div#chart-list div:not(.no-longer-listed) > div.listItem");
        List<ChartsItem> chartItems = new ArrayList<>(listItems.size());

                var chartName = getChartName(top40Page);
        var year = getYearOfChart(top40Page);
        var weekNumber = getWeekOfChart(top40Page);

        //start with index 1, because we need to skip the header row
        for (int i=1; i<listItems.size(); i++) {
            if (isChartEntry(listItems.get(i))) {
                try {
                    chartItems.add(getChartsItem(listItems.get(i), chartName, year, weekNumber));
                } catch (RuntimeException e) {
                    LOGGER.error(String.format("Error context [%d]:%n%s", i, listItems.get(i)), e);
                }
            }
        }
        LOGGER.info("Scraped week {} of {} {}", weekNumber, chartName, year);
        if (LOGGER.isDebugEnabled()) {
            chartItems.forEach(item -> LOGGER.debug(item.toString()));
        }
        return chartItems;
    }

    private boolean isChartEntry(Element itemElement) {
    	//a way to suppress 'no-longer-listed' items
        return !itemElement.selectFirst("div.dot-icon").text().equals("-");
    }

    byte getWeekOfChart(Document top40Page) {
        var title = top40Page.title();
        var matcher = WEEKNUMBER_PATTERN.matcher(title);
        if (matcher.find()) {
            return Byte.parseByte(matcher.group(1));
        }
        throw new IllegalStateException("Weeknumber of chart not found in title: " + title);
    }

    short getYearOfChart(Document top40Page) {
        //the page title ends with the 4 digit year
        var title = top40Page.title();
        return Short.parseShort(title.substring(title.length() - 4));
    }

    String getChartName(Document top40Page) {
        var chartNameElement = top40Page.selectFirst("ul.hitlist li.active h1");
        return chartNameElement.text();
    }

    private ChartsItem getChartsItem(Element itemElement, String chartName, short year, byte weekNumber) {
        var songTitle = itemElement.selectFirst("div.song-details h3.title").text();
        var artist = itemElement.selectFirst("div.song-details p.artist").text();
        var position = Byte.parseByte(itemElement.selectFirst("div.dot-icon").text());
        var isNewInChart = isNewInChart(itemElement);
        return new ChartsItem(chartName, year, weekNumber, position, isNewInChart, new Song(artist, songTitle));
    }

    private boolean isNewInChart(Element itemElement) {
        var statColumns = itemElement.select("div.statcolumn strong");
        //get the 'number of weeks in charts' item 
        return (statColumns.size() >= 2)? statColumns.get(1).text().equals("1"): false;
    }

    @Override
    public Optional<String> getUrlTemplate(MusicChart chart) {
         return SUPPORTED_CHARTS.contains(chart)? Optional.of(URL_TEMPLATE): Optional.empty();
    }
}
