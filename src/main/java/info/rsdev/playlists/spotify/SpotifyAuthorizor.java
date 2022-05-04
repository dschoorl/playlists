/*
 * Copyright 2022 Red Star Development.
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
package info.rsdev.playlists.spotify;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.exceptions.detailed.UnauthorizedException;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeRequest;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest;

/**
 * This class is responsible to manage the SpotifyApi authorizations, including obtaining and persisting
 * tokens.
 * 
 * @author Dave Schoorl
 */
@Component
public class SpotifyAuthorizor {

	private static final Logger LOGGER = LoggerFactory.getLogger(SpotifyAuthorizor.class);
	
	private static record AccessAndRefreshTokens(String accessToken, String refreshToken) {}

	@Resource
	private Environment env;
	
	public void authorize(SpotifyApi spotifyApi) throws UnauthorizedException {
		try {
			AccessAndRefreshTokens tokens = getAccessAndRefreshToken(spotifyApi);
			spotifyApi.setAccessToken(tokens.accessToken());
			spotifyApi.setRefreshToken(tokens.refreshToken());
		} catch (SpotifyWebApiException e) {
			if (e.getMessage().toLowerCase().contains("authorization")) {
				throw new UnauthorizedException("Could not get accessToken nor refreshToken", e);
			}
			throw new RuntimeException(e);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private AccessAndRefreshTokens getAccessAndRefreshToken(SpotifyApi spotifyApi) throws UnauthorizedException {
		var accessToken = env.getProperty("spotify.accessToken");
		var refreshToken = env.getProperty("spotify.refreshToken");
		if (StringUtils.hasText(accessToken) && StringUtils.hasText(refreshToken)) {
			//use current and existing tokens from properties file
			return new AccessAndRefreshTokens(accessToken, refreshToken);
		}
		
		return retrieveAccessAndRefreshToken(spotifyApi);
	}

	private AccessAndRefreshTokens retrieveAccessAndRefreshToken(SpotifyApi spotifyApi) throws UnauthorizedException {
		var authorizationCode = env.getProperty("spotify.authCode");
		if (!StringUtils.hasText(authorizationCode)) { 
			throw new RuntimeException(getAuthorizationCodeUrlMessage(spotifyApi)); 
		}
		
		AuthorizationCodeRequest authorizationCodeRequest = spotifyApi.authorizationCode(authorizationCode).build();
		try {
			AuthorizationCodeCredentials authorizationCodeCredentials = authorizationCodeRequest.execute();
			// Set access and refresh token for further "spotifyApi" object usage
			var accessToken = authorizationCodeCredentials.getAccessToken();
			var refreshToken = authorizationCodeCredentials.getRefreshToken();
			LOGGER.info("spotify.accessToken={}", accessToken);
			LOGGER.info("spotify.refreshToken={}", refreshToken);
			LOGGER.info("accessToken expires in {} sec.", authorizationCodeCredentials.getExpiresIn());
			//TODO: persist values in property file, so they can be reused after application restart
			return new AccessAndRefreshTokens(accessToken, refreshToken);
		} catch (SpotifyWebApiException e) {
			if (e.getMessage().toLowerCase().contains("authorization")) {
				throw new UnauthorizedException(getAuthorizationCodeUrlMessage(spotifyApi), e);
			}
			throw new RuntimeException(e);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public String getAuthorizationCodeUrlMessage(SpotifyApi spotifyApi) {
    	try {
	    	AuthorizationCodeUriRequest authorizationCodeUriRequest = spotifyApi.authorizationCodeUri()
	              .scope("playlist-read-private playlist-modify-private playlist-modify user-read-private user-read-email")
	              .build();
	    	return String.format("Get your authCode through your web browser at:%n%s%n", authorizationCodeUriRequest.execute());
    	} catch (Exception e) {
    		throw new RuntimeException(e);
    	}
	}


}
