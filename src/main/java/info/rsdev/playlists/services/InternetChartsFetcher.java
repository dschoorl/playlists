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

import java.io.IOException;
import java.util.Optional;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * This implementation of a {@link DocumentFetcher} knows how to read the html from the top40.nl web site and how to process
 * the {@link MusicChart}s that are hosted there.
 */
public class InternetChartsFetcher implements DocumentFetcher {

    private static final String USERAGENT_STRING = "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:59.0) Gecko/20100101 Firefox/59.0";

    private final String urlString;

    public InternetChartsFetcher(String urlTemplate, Object... parameters) {
        urlString = String.format(urlTemplate, parameters);
    }

    @Override 
    public Optional<Document> fetch() {
        try {
            return Optional.of(Jsoup.connect(getLocation())
                    .userAgent(USERAGENT_STRING)
                    .get());
        } catch (HttpStatusException e) {
        	if (e.getStatusCode() == 404) {
        		return Optional.empty();
        	}
        	throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override public String getLocation() {
        return urlString;
    }

}
