package info.rsdev.playlists.testutils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import info.rsdev.playlists.services.DocumentFetcher;

public class TestfileDocumentFetcher implements DocumentFetcher {

    private final Path fileLocation;

    public TestfileDocumentFetcher(Path fileLocation) {
        this.fileLocation = fileLocation;
    }

    @Override 
    public Optional<Document> fetch() {
        try {
            return Optional.of(Jsoup.parse(fileLocation.toFile(), null));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override public String getLocation() {
        return fileLocation.toString();
    }
}
