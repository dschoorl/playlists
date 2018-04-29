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

import java.util.List;
import java.util.Optional;

import info.rsdev.playlists.domain.ChartsPlaylist;
import info.rsdev.playlists.domain.Song;
import info.rsdev.playlists.domain.SongFromCatalog;

public interface MusicCatalogService {
	
	Optional<SongFromCatalog> findSong(Song song);
	
	ChartsPlaylist getOrCreatePlaylist(String playlistName);
	
	void addToPlaylist(ChartsPlaylist playlist, List<SongFromCatalog> songs);

}
