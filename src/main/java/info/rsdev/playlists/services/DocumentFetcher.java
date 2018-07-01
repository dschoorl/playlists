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

import java.util.Optional;

import org.jsoup.nodes.Document;

/**
 * This interface defines the interactions to obtain html content from an URL as a {@link Document}. It is capable 
 * of obtaining all information, even in case the information is paginated (@see {@link #hasNext()})
 */
public interface DocumentFetcher {

    /**
     * Retrieve the information at the given location
     * @return the {@link Document} when it could be retrieved, wrapped in an {@link Optional}, otherwise an empty optional
     */
    Optional<Document> fetch();

    /**
     * Get a string representation of the given location of this instance
     * @return a String representing the location
     */
    String getLocation();

    /**
     * Determine if the information is paginated and there are more pages with information available.
     * @return true if there is more information available with a call to {@link #fetchNext()}, false otherwise
     */
    default boolean hasNext() {
        return false;
    }

    default Optional<Document> fetchNext() {
        return Optional.empty();
    };

}
