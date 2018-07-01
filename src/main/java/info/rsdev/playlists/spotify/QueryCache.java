package info.rsdev.playlists.spotify;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import info.rsdev.playlists.domain.SongFromCatalog;

public class QueryCache {
    
    private final File cache;
    
    private final ConcurrentHashMap<String, SongFromCatalog> cacheItems;
    
    public QueryCache() {
        cache = new File(new File(new File(System.getProperty("user.home")), ".playlists"), "playlist.cache");
        cacheItems = deserializeOrCreateCache();
    }
    
    public void cache(String searchCriteria, SongFromCatalog result) {
        this.cacheItems.put(searchCriteria, result);
    }
    
    public Optional<SongFromCatalog> getFromCache(String searchCriteria) {
        return Optional.ofNullable(this.cacheItems.get(searchCriteria));
    }
    
    @SuppressWarnings("unchecked")
    private ConcurrentHashMap<String, SongFromCatalog> deserializeOrCreateCache() {
        if (!cache.isFile()) {
            return new ConcurrentHashMap<>();
        }
        
        try (ObjectInputStream reader = new ObjectInputStream(new BufferedInputStream(new FileInputStream(cache)))) {
            return (ConcurrentHashMap<String, SongFromCatalog>) reader.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    
    public void writeCache() {
        try (ObjectOutputStream writer = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(cache)))) {
            writer.writeObject(cacheItems);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public int size() {
        return cacheItems.size();
    }

}
