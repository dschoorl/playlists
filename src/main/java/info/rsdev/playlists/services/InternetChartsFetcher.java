package info.rsdev.playlists.services;

import java.io.IOException;
import java.util.Optional;

import org.apache.http.HttpStatus;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.rsdev.playlists.exception.FailedHostException;

public record InternetChartsFetcher(String location) implements DocumentFetcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(InternetChartsFetcher.class);

    private static final String ABORT_MSG = "Website down? Abort scraping to prevent holes in the chart data.";
    
    private static final String USERAGENT_STRING = "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:109.0) Gecko/20100101 Firefox/117.0";

    @Override
    public Optional<Document> fetch() {
        long start = System.currentTimeMillis();
        try {
            return Optional.ofNullable(Jsoup.connect(location()).userAgent(USERAGENT_STRING).get());
        } catch (HttpStatusException e) {
            if (e.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
                // we might query a non-existent week, e.g. week 53
                LOGGER.error("Could not fetch data from {}: {}", location(), e.getMessage());
                return Optional.empty();
            }
            throw new FailedHostException(ABORT_MSG, e);
        } catch (IOException e) {
            throw new FailedHostException(ABORT_MSG, e);
        } finally {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Fetched {} in {} ms.", location(), System.currentTimeMillis() - start);
            }
        }
    }
}
