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
package info.rsdev.playlists.ioc

import java.net.URLEncoder

import javax.inject.Inject

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.PropertySource
import org.springframework.core.env.Environment
import org.springframework.util.StringUtils

import com.wrapper.spotify.exceptions.detailed.UnauthorizedException

import info.rsdev.playlists.dao.ChartsItemDao
import info.rsdev.playlists.services.MusicCatalogService
import info.rsdev.playlists.services.MusicChartsService
import info.rsdev.playlists.services.MusicTitleService
import info.rsdev.playlists.services.ScrapeService
import info.rsdev.playlists.services.SpotifyCatalogService
import info.rsdev.playlists.services.Top40ScrapeService
import kotlin.String.Companion

/**
 *
 * @author Dave Schoorl
 */
@Configuration
@PropertySource(value = ["file:\${user.home}/.playlists/spotify.properties"])
@Import(SpringDatalayerConfig::class)
class SpringCommonConfig {

    @Inject
    internal var env: Environment? = null

    private val accessTokenUrlMessage: String
        get() {
            val clientId = env!!.getRequiredProperty("spotify.clientId")
            val redirectUrl = URLEncoder.encode(env!!.getRequiredProperty("spotify.redirectUrl"), "UTF-8")
            return String.format("Get your accessToken through your web browser at:%n"
                    + "https://accounts.spotify.com/authorize" +
                    "?response_type=token&client_id=%s&redirect_uri=%s&scope=playlist-read-private%%20playlist-modify-private%%20playlist-modify%n", clientId, redirectUrl)
        }

    @Bean
    internal fun singleService(scrapeService: ScrapeService, chartsItemDao: ChartsItemDao): MusicTitleService {
        return MusicChartsService(scrapeService, chartsItemDao)
    }

    @Bean
    internal fun scrapeService(): ScrapeService {
        return Top40ScrapeService()
    }

    @Bean
    internal fun catalogService(): MusicCatalogService? {
        val clientId = env!!.getRequiredProperty("spotify.clientId")
        val clientSecret = env!!.getRequiredProperty("spotify.clientSecret")
        val accessToken = env!!.getProperty("spotify.accessToken")
        if (StringUtils.isEmpty(accessToken)) {
            throw RuntimeException(accessTokenUrlMessage)
        }
        val refreshToken = env!!.getProperty("spotify.refreshToken")  //currently not supported / needed
        var catalogService: SpotifyCatalogService? = null
        try {
            catalogService = SpotifyCatalogService(clientId, clientSecret, accessToken!!, refreshToken!!)
        } catch (e: RuntimeException) {
            handleRuntimeException(e)
        }

        return catalogService
    }

    private fun handleRuntimeException(e: RuntimeException) {
        if (e.cause is UnauthorizedException) {
            throw IllegalStateException(accessTokenUrlMessage, e)
        }
        throw e
    }
}
