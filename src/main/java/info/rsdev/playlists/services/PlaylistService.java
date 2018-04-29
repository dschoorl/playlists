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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.rsdev.playlists.domain.ChartsPlaylist;
import info.rsdev.playlists.domain.Song;
import info.rsdev.playlists.domain.SongFromCatalog;

public class PlaylistService {
	
    private static final Logger LOGGER = LoggerFactory.getLogger(PlaylistService.class);

    @Inject 
    private MusicCatalogService catalogService;
    
	public void makePlaylistWithSongs(String playlistName, List<Song> songs) {
		
		ChartsPlaylist playlist = catalogService.getOrCreatePlaylist(playlistName);
		List<SongFromCatalog> songsFromCatalog = new ArrayList<>(songs.size());
		int notFound = 0;
        for (Song song : songs) {
        	Optional<SongFromCatalog> catalogSong = catalogService.findSong(song);
        	if (!catalogSong.isPresent()) {
        		LOGGER.info(String.format("%s - %s", song, catalogSong.map(item -> item.trackUri).orElse("Not found")));
        		notFound++;
        	}
        	
        	catalogSong.ifPresent(fromCatalog -> songsFromCatalog.add(fromCatalog));
        }
        
        if (notFound > 0) {
        	LOGGER.warn("Total # not found on spotify: " + notFound);
        }
        
        catalogService.addToPlaylist(playlist, songsFromCatalog);
	}
	
}
