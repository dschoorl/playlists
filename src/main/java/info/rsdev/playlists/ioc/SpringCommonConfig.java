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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import javax.inject.Inject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import com.wrapper.spotify.exceptions.detailed.UnauthorizedException;

import info.rsdev.playlists.dao.ChartsItemDao;
import info.rsdev.playlists.services.MusicCatalogService;
import info.rsdev.playlists.services.MusicChartsService;
import info.rsdev.playlists.services.MusicTitleService;
import info.rsdev.playlists.services.ScrapeService;
import info.rsdev.playlists.spotify.SpotifyCatalogService;
import info.rsdev.playlists.services.Top40ScrapeService;

/**
 *
 * @author Dave Schoorl
 */
@Configuration
@PropertySource(value = "file:${user.home}/.playlists/spotify.properties")
@Import(SpringElasticsearchConfig.class)
public class SpringCommonConfig {

    @Inject
    private Environment env;

    private String getAccessTokenUrlMessage() throws UnsupportedEncodingException {
        var clientId = env.getRequiredProperty("spotify.clientId");
        var redirectUrl = URLEncoder.encode(env.getRequiredProperty("spotify.redirectUrl"), "UTF-8");
        return String.format("Get your accessToken through your web browser at:%n"
                + "https://accounts.spotify.com/authorize" +
                "?response_type=token&client_id=%s&redirect_uri=%s&scope=playlist-read-private%%20playlist-modify-private%%20playlist-modify%%20user-read-private%%20user-read-email%n", clientId, redirectUrl);
    }

    @Bean
    public MusicTitleService singleService(ScrapeService scrapeService, ChartsItemDao chartsItemDao) {
        return new MusicChartsService(scrapeService, chartsItemDao);
    }

    @Bean
    public ScrapeService scrapeService() {
        return new Top40ScrapeService();
    }

    @Bean
    public MusicCatalogService catalogService() throws UnsupportedEncodingException {
        var clientId = env.getRequiredProperty("spotify.clientId");
        var clientSecret = env.getRequiredProperty("spotify.clientSecret");
        var accessToken = env.getRequiredProperty("spotify.accessToken");
        if (!StringUtils.hasText(accessToken)) {
            throw new RuntimeException(getAccessTokenUrlMessage());
        }
        var refreshToken = env.getProperty("spotify.refreshToken");  //currently not supported / needed
        try {
            return new SpotifyCatalogService(clientId, clientSecret, accessToken, refreshToken);
        } catch (UnauthorizedException e) {
            throw new IllegalStateException(getAccessTokenUrlMessage(), e);
        }
    }
}
