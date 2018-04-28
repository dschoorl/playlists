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
package info.rsdev.playlists.ioc;

import javax.inject.Inject;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import com.wrapper.spotify.exceptions.detailed.UnauthorizedException;

import info.rsdev.playlists.dao.ChartsItemDao;
import info.rsdev.playlists.services.MusicCatalogService;
import info.rsdev.playlists.services.MusicChartsService;
import info.rsdev.playlists.services.MusicTitleService;
import info.rsdev.playlists.services.ScrapeService;
import info.rsdev.playlists.services.SpotifyCatalogService;
import info.rsdev.playlists.services.Top40ScrapeService;

/**
 *
 * @author Dave Schoorl
 */
@Configuration
@PropertySource(value = "file:${user.home}/.playlists/spotify.properties")
@Import({SpringDatalayerConfig.class})
public class SpringCommonConfig {

    @Inject
    Environment env;

    @Bean
    MusicTitleService singleService(ScrapeService scrapeService, ChartsItemDao chartsItemDao) {
        return new MusicChartsService(scrapeService, chartsItemDao);
    }

    @Bean
    ScrapeService scrapeService() {
        return new Top40ScrapeService();
    }
    
    @Bean
    MusicCatalogService catalogService() {
        String clientId = env.getRequiredProperty("spotify.clientId");
        String clientSecret = env.getRequiredProperty("spotify.clientSecret");
        String accessToken = env.getProperty("spotify.accessToken");
        String refreshToken = env.getProperty("spotify.refreshToken");
        SpotifyCatalogService catalogService = null;
        try {
        	catalogService = new SpotifyCatalogService(clientId, clientSecret, accessToken, refreshToken);
        } catch (RuntimeException e) {
        	handleRuntimeException(e);
        }
        return catalogService;
    }

	private void handleRuntimeException(RuntimeException e) {
		if (e.getCause() instanceof UnauthorizedException) {
	        String clientId = env.getRequiredProperty("spotify.clientId");
        	String message = String.format("Get your access- and refreshToken through your web browser at:%nhttps://accounts.spotify.com/authorize" +
        			"?response_type=token&client_id=%s&redirect_uri=https%%3A%%2F%%2Frsdev.info&scope=playlist-read-private%%20playlist-modify-private%%20playlist-modify%n", clientId);
//        	byte[] basicAuth = (clientId + ":" + clientSecret).getBytes();
//        	message += String.format("Use Authorization header with value: %s", Base64.getEncoder().encodeToString(basicAuth));
        	throw new IllegalStateException(message, e);
		}
		throw e;
	}
}
