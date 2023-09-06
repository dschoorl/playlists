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

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import info.rsdev.playlists.domain.ChartsItem;
import info.rsdev.playlists.exception.FailedHostException;

public interface ScrapeService {

    default List<MusicChart> getSupportedCharts() {
        return Collections.emptyList();
    }

    /**
     * Transform the html that is retrieved via the [DocumentFetcher] into a list of
     * [ChartsItem]s
     * 
     * @param fetcher the [DocumentFetcher] pointing at a html source
     * @return the [ChartsItem]s that are listed in the html
     * @throws FailedHostException When host to scrape is not operating correctly or
     *                             is unreachable
     */
    List<ChartsItem> scrape(DocumentFetcher fetcher) throws FailedHostException;

    Optional<String> getUrlTemplate(MusicChart chart);
}
