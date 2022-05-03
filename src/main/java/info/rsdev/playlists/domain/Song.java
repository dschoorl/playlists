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
package info.rsdev.playlists.domain;


import java.io.Serializable;

/**
 * This class represents a Song with a [title] that is performed by an [artist]. The natural order of Songs is alphabetical by
 * [artist] and then alphabetical by [title]. A song can be cached to file by [info.rsdev.playlists.spotify.QueryCache] and
 * therefore needs to implement [Serializable].
 */
public record Song(String artist, String title) implements Serializable, Comparable<Song> {

    @Override
    public int compareTo(Song other) {
        if (artist.equals(other.artist)) {
            return title.compareTo(other.title);
        }
        return artist.compareTo(other.artist);
    }
}
