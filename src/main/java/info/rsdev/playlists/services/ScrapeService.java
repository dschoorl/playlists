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

import java.util.List;
import java.util.Optional;

/**
 * This service is responsible for scraping a web page containing music charts for a single period, most often a week.
 */
public interface ScrapeService {

    /**
     * Transform the html that is retrieved via the {@link DocumentFetcher} into a list of {@link ChartsItem}s
     * @param fetcher the {@link DocumentFetcher} pointing at a html source
     * @return the {@link ChartsItem}s that are listed in the html
     */
    List<ChartsItem> scrape(DocumentFetcher fetcher);
    
    List<MusicChart> getSupportedCharts();
    
    Optional<String> getUrlTemplate(MusicChart chart);
    
}
