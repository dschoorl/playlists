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
package info.rsdev.playlists.domain

import java.io.Serializable

data class Song(val artist: String, val title: String) : Comparable<Song>, Serializable {

    override fun compareTo(other: Song): Int {
        return if (artist == other.artist) {
            title.compareTo(other.title)
        } else artist.compareTo(other.artist)
    }
}
