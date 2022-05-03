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
import java.net.URI;
import java.net.URISyntaxException;

import javax.annotation.Resource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import info.rsdev.playlists.services.MusicCatalogService;
import info.rsdev.playlists.services.ScrapeService;
import info.rsdev.playlists.services.Top40ScrapeService;
import info.rsdev.playlists.spotify.SpotifyCatalogService;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.detailed.UnauthorizedException;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest;

/**
 *
 * @author Dave Schoorl
 */
@Configuration
@PropertySource(value = "file:${user.home}/.playlists/spotify.properties")
@Import(SpringElasticsearchConfig.class)
public class SpringCommonConfig {

	@Resource
	private Environment env;

	private String getAuthorizationCodeUrlMessage() throws UnsupportedEncodingException {
		SpotifyApi spotifyApi = spotifyApi();
    	try {
	    	AuthorizationCodeUriRequest authorizationCodeUriRequest = spotifyApi.authorizationCodeUri()
	              .scope("playlist-read-private playlist-modify-private playlist-modify user-read-private user-read-email")
	              .build();
	    	return String.format("Get your authCode through your web browser at:%n%s%n", authorizationCodeUriRequest.execute());
    	} catch (Exception e) {
    		throw new RuntimeException(e);
    	}
	}

	@Bean
	public ScrapeService scrapeService() {
		return new Top40ScrapeService();
	}

	@Bean
	public SpotifyApi spotifyApi() {
		var clientId = env.getRequiredProperty("spotify.clientId");
		var clientSecret = env.getRequiredProperty("spotify.clientSecret");
		try {
			var redirectUrl = new URI(env.getRequiredProperty("spotify.redirectUrl"));
			return SpotifyApi.builder()
					.setClientId(clientId)
					.setClientSecret(clientSecret)
					.setRedirectUri(redirectUrl)
					.build();
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	@Bean
	public MusicCatalogService catalogService() throws UnsupportedEncodingException {
		var authorizationCode = env.getRequiredProperty("spotify.authCode");
		if (!StringUtils.hasText(authorizationCode)) { throw new RuntimeException(getAuthorizationCodeUrlMessage()); }
		try {
			return new SpotifyCatalogService(spotifyApi(), authorizationCode);
		} catch (UnauthorizedException e) {
			throw new IllegalStateException(getAuthorizationCodeUrlMessage(), e);
		}
	}
}
