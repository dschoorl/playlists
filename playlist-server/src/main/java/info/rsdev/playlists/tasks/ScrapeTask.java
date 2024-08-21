/*
 * Copyright 2024 Red Star Development.
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
 */package info.rsdev.playlists.tasks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import info.rsdev.playlists.Playlists;
import info.rsdev.playlists.services.MusicTitleService;
import jakarta.annotation.Resource;

@Component
public class ScrapeTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(Playlists.class);
    
    @Resource
    private MusicTitleService titleService;
    
    @Scheduled(cron = "* 0 22 * * Thu")
//    @Scheduled(fixedRate = 5, timeUnit = TimeUnit.MINUTES)
    public void scrapeCharts() throws Exception {
        LOGGER.info("Running scheduled task to scrape music charts");
        var startTime = System.currentTimeMillis();
        titleService.init();
        LOGGER.info("Music charts scraped after {}s", (System.currentTimeMillis() - startTime) / 1000);
    }

}
