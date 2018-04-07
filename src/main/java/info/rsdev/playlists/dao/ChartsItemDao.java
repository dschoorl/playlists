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
package info.rsdev.playlists.dao;

import java.util.Collection;

import info.rsdev.playlists.domain.ChartsItem;
import info.rsdev.playlists.domain.Song;

public interface ChartsItemDao {

    /**
     * prepare an existing datastore to persist {@link ChartsItem} instances. If the data store is already
     * setup, this method will do nothing
     * @return true if the persistence store was newly created, false otherwise
     */
    boolean setupStoreWhenNeeded();

    void saveOrUpdate(ChartsItem chartsItem);
    
    Collection<Song> getReleases(short year);
    
}
