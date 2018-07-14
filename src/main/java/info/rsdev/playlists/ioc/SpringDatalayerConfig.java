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

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import info.rsdev.playlists.dao.ChartsItemDao;
import info.rsdev.playlists.dao.ElasticBulkWritingChartsItemDao;

/**
 *
 * @author Dave Schoorl
 */
@Configuration
@PropertySource(value = "file:${user.home}/.playlists/elasticsearch.properties")
public class SpringDatalayerConfig {
	
	@Inject
	Environment env;
	
    @Bean
    public ChartsItemDao chartsItemDao(RestHighLevelClient elasticClient) {
        return new ElasticBulkWritingChartsItemDao(elasticClient);
    }

    @Bean
    public RestHighLevelClient elasticClient() {
    	String hostname = env.getProperty("es.hostname", "localhost");
    	int portnumber = Integer.parseInt(env.getProperty("es.portnumber", "9200"));
        return new RestHighLevelClient(RestClient.builder(new HttpHost(hostname, portnumber, "http")));
    }
    
}
