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
package info.rsdev.playlists.services

import org.jsoup.nodes.Document
import java.util.*

/**
 * This interface defines the interactions to obtain html content from an URL as a [org.jsoup.nodes.Document]. It is capable
 * of obtaining all information, even in case the information is paginated (@see {@link #hasNext()})
 */
interface DocumentFetcher {

    /**
     * Retrieve the information at the given location
     * @return the [Document] when it could be retrieved, null otherwise
     */
    fun fetch() : Document?

    /**
     * Get a string representation of the given location of this instance
     * @return a String representing the location
     */
    fun getLocation() : String

    /**
     * Determine if the information is paginated and there are more pages with information available.
     * @return true if there is more information available with a call to [.fetchNext], false otherwise
     */
    fun hasNext() = false

    fun fetchNext() : Document? = null
}
