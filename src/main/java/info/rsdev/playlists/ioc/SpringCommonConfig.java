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

import java.util.Base64;

import javax.inject.Inject;

import org.apache.logging.log4j.util.Strings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import info.rsdev.playlists.dao.ChartsItemDao;
import info.rsdev.playlists.services.MusicCatalogService;
import info.rsdev.playlists.services.MusicChartsService;
import info.rsdev.playlists.services.ScrapeService;
import info.rsdev.playlists.services.SingleService;
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
    SingleService singleService(ScrapeService scrapeService, ChartsItemDao chartsItemDao) {
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
        if (Strings.isBlank(accessToken) && Strings.isBlank(refreshToken)) {
        	String message = String.format("Get your access- and refreshToken through your web browser at:%nhttps://accounts.spotify.com/authorize" +
        			"?response_type=token&client_id=%s&redirect_uri=https%%3A%%2F%%2Frsdev.info&scope=playlist-read-private%%20playlist-modify-private%%20playlist-modify%n", clientId);
        	byte[] basicAuth = (clientId + ":" + clientSecret).getBytes();
        	message += String.format("Use Authorization header with value: %s", Base64.getEncoder().encodeToString(basicAuth));
        	throw new IllegalStateException(message);
        	//code = AQCUyAPg2g5_tJ_7m-TbnbjxO_VIYKLve4VvBJwGvQfQowWPcCOFsDfSoqPkZVX7RAoJcUK7oZO1UKlN3JAUgnAzGzhiW-CHu-OkYB_JsByFJWK9_Ezr_ArXb_wZrbxnTDk-dFUKXxYyB-dLDbpupgdm_PObsadeW7S96JLjTZkvsCCWXI-AKogdD9olYokMthdI98AzQPmnSwmtgUjxvTsoSUnj75cwG6vtOG9IwX9yi25bxb3XnQ
        	//token = 
        }
        return new SpotifyCatalogService(clientId, clientSecret, accessToken, refreshToken);
    }
}
