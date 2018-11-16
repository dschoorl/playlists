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
package info.rsdev.playlists.testutils

import info.rsdev.playlists.services.DocumentFetcher
import org.jsoup.Jsoup
import java.nio.file.Path
import java.util.*

class TestFileDocumentFetcher(private val fileLocation: Path) : DocumentFetcher {

    override fun fetch() = Optional.of(Jsoup.parse(fileLocation.toFile(), null))

    override fun getLocation() = fileLocation.toString()
}
