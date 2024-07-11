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
package info.rsdev.playlists.spotify;

import info.rsdev.playlists.domain.Song;
import info.rsdev.playlists.domain.SongFromCatalog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cache search results for a Song
 */
public class QueryCache {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueryCache.class);
    private final File cache = new File(new File(new File(System.getProperty("user.home")), ".playlists"), "playlist.cache");

    private final ConcurrentHashMap<Song, SongFromCatalog> cacheItems;

    public QueryCache() {
        cacheItems = deserializeOrCreateCache();
    }

    private ConcurrentHashMap<Song, SongFromCatalog> deserializeOrCreateCache() {
        if (!cache.isFile()) {
            return new ConcurrentHashMap<>();
        }

        try (ObjectInputStream reader = new ObjectInputStream(new BufferedInputStream(new FileInputStream(cache)))) {
            return (ConcurrentHashMap<Song, SongFromCatalog>) reader.readObject();
        } catch (IOException | ClassNotFoundException e) {
            LOGGER.error("Starting with empty cache, because QueryCache could not be deserialized from file", e);
            return new ConcurrentHashMap<>();
        }
    }

    public void cache(Song song, SongFromCatalog result) {
        this.cacheItems.put(song, result);
    }

    public Optional<SongFromCatalog> getFromCache(Song song) {
     return Optional.ofNullable(this.cacheItems.getOrDefault(song, null));
    }

    public void writeCache() {
        try (ObjectOutputStream writer = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(cache)))) {
            writer.writeObject(cacheItems);
        } catch (IOException e) {
            LOGGER.error("Could not write QueryCache to file", e);
        }
    }

    public int size() {
        return cacheItems.size();
    }
}
