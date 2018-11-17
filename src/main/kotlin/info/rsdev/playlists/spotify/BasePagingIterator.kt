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
package info.rsdev.playlists.spotify

import com.wrapper.spotify.SpotifyApi
import com.wrapper.spotify.model_objects.specification.Paging

/**
 *
 * @param <T> the object type to iterate over
</T> */
abstract class BasePagingIterator<T>(protected val spotifyApi: SpotifyApi) : Iterator<T> {
    private var results: Paging<T>? = null
    var size: Int = 0
        private set
    private var pageItemsIterated: Int = 0
    private var totalItemsIterated = 0

    protected fun initialize() {
        results = getResults()
        size = results!!.total!!
    }

    private fun getResults(): Paging<T> {
        this.pageItemsIterated = 0
        return getResults(totalItemsIterated)
    }

    protected abstract fun getResults(offset: Int): Paging<T>

    override fun hasNext(): Boolean {
        return totalItemsIterated < size
    }

    override fun next(): T {
        val nextItem = results!!.items[pageItemsIterated]
        pageItemsIterated++
        totalItemsIterated++

        //When we have read the last item on this page, but there are more pages, load the next page now
        if (results!!.items.size == pageItemsIterated && hasNext()) {
            results = getResults()
        }
        return nextItem
    }

}
