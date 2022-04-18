package info.rsdev.playlists.services;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.Optional;

public record InternetChartsFetcher(String location) implements DocumentFetcher {

    private static final String USERAGENT_STRING = "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:59.0) Gecko/20100101 Firefox/59.0";

    @Override
    public Optional<Document> fetch() {
        try {
            return Optional.ofNullable(Jsoup.connect(location())
                    .userAgent(USERAGENT_STRING)
                    .get());
        } catch (IOException e) {
            return Optional.empty();
        }
    }
}
