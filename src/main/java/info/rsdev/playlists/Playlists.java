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
package info.rsdev.playlists;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import info.rsdev.playlists.domain.ChartsPlaylist;
import info.rsdev.playlists.ioc.SpringPlaylistsConfig;
import info.rsdev.playlists.services.MusicCatalogService;
import info.rsdev.playlists.services.SingleService;

/**
 *
 * @author Dave Schoorl
 */
public class Playlists {

    private static final Logger LOGGER = LoggerFactory.getLogger(Playlists.class);

    @Inject
    private SingleService singleService;
    
    @Inject MusicCatalogService catalogService;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
    	try (ConfigurableApplicationContext iocContext = new AnnotationConfigApplicationContext(SpringPlaylistsConfig.class)) {
	        Playlists playlists = iocContext.getBean(Playlists.class);
	        playlists.start(args);
    	}
    }

    private void start(String[] args) {
    	long startTime = System.currentTimeMillis();
        singleService.init();
        LOGGER.info(String.format("Finished: %ds", (System.currentTimeMillis() - startTime)/1000));
        
        ChartsPlaylist chartsOf2018 = catalogService.getOrCreatePlaylist((short)2018);
        LOGGER.info(String.format("Playlist exists: '%s'", chartsOf2018.name));
    }

}
