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
package info.rsdev.playlists.config;

import java.net.URI;
import java.net.URISyntaxException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import jakarta.annotation.Resource;
import se.michaelthelin.spotify.SpotifyApi;

/**
 *
 * @author Dave Schoorl
 */
@Configuration
@PropertySource(value = "file:${user.home}/.playlists/spotify.properties")
@Profile("spotify")
public class SpringCommonConfig {

	@Resource
	private Environment env;

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
}
