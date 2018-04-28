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
package info.rsdev.playlists.dao;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;

import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.rsdev.playlists.domain.ChartsItem;
import info.rsdev.playlists.domain.Song;

public class ElasticBulkWritingChartsItemDao implements ChartsItemDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticBulkWritingChartsItemDao.class);

    private static final String CHARTSITEM_INDEX_NAME = "chartitems";
    private static final String CHARTSITEM_DOCTYPE = "chartitem";
    
    private static final String CHARTNAME = "chartName";
    private static final String YEAR = "year";
    private static final String WEEK = "weekNumber";
    private static final String POSITION = "position";
    private static final String IS_NEW_IN_CHART = "isNewRelease";
    private static final String ARTIST = "artist";
    private static final String TITLE = "title";
    
    
    private static final int RETURN_NO_DOCUMENTS = 0;
    private static final int ROUNDUP_FACTOR = 100;

    private final RestHighLevelClient elasticsearchClient;

    private final BulkProcessor bulkProcessor;

    /* This class contains callback methods that are called prior to sending a bulk request and on the bulk response,
     * success or failure.
     */
    private BulkProcessor.Listener listener = new BulkProcessor.Listener() {

        @Override public void beforeBulk(long executionId, BulkRequest request) {

        }

        @Override public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {

        }

        @Override public void afterBulk(long executionId, BulkRequest request, Throwable failure) {

        }
    };

    @Inject
    public ElasticBulkWritingChartsItemDao(RestHighLevelClient elasticClient) {
        this.elasticsearchClient = elasticClient;
        this.bulkProcessor = BulkProcessor.builder(elasticClient::bulkAsync, listener)
                .setBulkActions(-1)
                .setBulkSize(new ByteSizeValue(5L, ByteSizeUnit.MB))
                .setConcurrentRequests(3)
                .setFlushInterval(TimeValue.timeValueSeconds(10L))
                .build();
    }

    @Override
    public boolean setupStoreWhenNeeded() {
        if (!doesChartsItemIndexExist()) {
            createChartsItemIndex();
            return true;
        }
        return false;
    }

    private boolean doesChartsItemIndexExist() {
        // Implemented with the low level REST client, because the high level client (v6.2.2) not (yet) supports the IndicesExistsRequest
        try {
            Response response = elasticsearchClient.getLowLevelClient().performRequest("HEAD", CHARTSITEM_INDEX_NAME);
            return response.getStatusLine().getStatusCode() == 200;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void createChartsItemIndex() {
        CreateIndexRequest request = new CreateIndexRequest(CHARTSITEM_INDEX_NAME);
        request.settings(Settings.builder()
                .put("index.number_of_shards", 1)
                .put("index.number_of_replicas", 0));

        request.mapping(CHARTSITEM_DOCTYPE, getChartsItemMapping());

        CreateIndexResponse response;
        try {
            response = elasticsearchClient.indices().create(request);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (!response.isAcknowledged()) {
            throw new RuntimeException(String.format("Creation of index '%s' is not acknowledged", CHARTSITEM_INDEX_NAME));
        }
        LOGGER.info("Created elasticsearch index " + CHARTSITEM_INDEX_NAME);
    }

    private XContentBuilder getChartsItemMapping() {
        try {
            return XContentFactory.jsonBuilder()
                .startObject()
                    .startObject(CHARTSITEM_DOCTYPE)
                        .startObject("properties")
                            .startObject(CHARTNAME).field("type", "keyword").endObject()
                            .startObject(YEAR).field("type", "short").endObject()
                            .startObject(WEEK).field("type", "byte").endObject()
                            .startObject(POSITION).field("type", "byte").endObject()
                            .startObject(IS_NEW_IN_CHART).field("type", "boolean").endObject()
                            .startObject(ARTIST).field("type", "text").endObject()
                            .startObject(TITLE).field("type", "text").endObject()
                        .endObject()
                    .endObject()
                .endObject();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void saveOrUpdate(ChartsItem chartsItem) {
        UpdateRequest request = new UpdateRequest(CHARTSITEM_INDEX_NAME, CHARTSITEM_DOCTYPE, UUID.randomUUID().toString());
        request.docAsUpsert(true);
        request.doc(toContentBuilder(chartsItem));
        this.bulkProcessor.add(request);
    }

    private XContentBuilder toContentBuilder(ChartsItem chartsItem) {
        try {
            return XContentFactory.jsonBuilder()
                .startObject()
                    .field(CHARTNAME, chartsItem.chartName)
                    .field(YEAR, chartsItem.year)
                    .field(WEEK, chartsItem.weekNumber)
                    .field(POSITION, chartsItem.position)
                    .field(IS_NEW_IN_CHART, chartsItem.isNewRelease)
                    .field(ARTIST, chartsItem.song.artist)
                    .field(TITLE, chartsItem.song.title)
                .endObject();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void close() {
        this.bulkProcessor.close();
    }

	@Override
	public Collection<Song> getReleases(short year) {
		int expectedResults = getReleasesCount(year, ROUNDUP_FACTOR);
		if (expectedResults == 0) {
			return Collections.emptyList();
		}
		
        SearchRequest searchRequest = makeSearchRequestReleasesByYear(year, expectedResults);
        try {
            SearchResponse searchResponse = elasticsearchClient.search(searchRequest);
            Set<Song> releases = new LinkedHashSet<>();
            searchResponse.getHits().forEach(searchHit -> releases.add(makeSong(searchHit.getSourceAsMap())));
            return releases;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
	}

	private Song makeSong(Map<String, Object> properties) {
		String artist = (String)properties.get(ARTIST);
		String title = (String)properties.get(TITLE);
		return new Song(artist, title);
	}

	private int getReleasesCount(short year, int roundFactor) {
        SearchRequest searchRequest = makeSearchRequestReleasesByYear(year, RETURN_NO_DOCUMENTS);
        try {
            SearchResponse searchResponse = elasticsearchClient.search(searchRequest);
            long elasticSearchResultCount = searchResponse.getHits().getTotalHits();
            if (elasticSearchResultCount == 0) {
	        	return 0;
	        }
            elasticSearchResultCount = ((elasticSearchResultCount / roundFactor) + 1) * roundFactor;
            if (elasticSearchResultCount > Integer.MAX_VALUE) {
            	throw new IllegalStateException("totalhits > Integer.MAX_VALUE is not supported");
            }
            return (int)elasticSearchResultCount;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
	}
	
	private SearchRequest makeSearchRequestReleasesByYear(short year, int limitResults) {
        SearchRequest searchRequest = new SearchRequest(CHARTSITEM_INDEX_NAME);
        searchRequest.types(CHARTSITEM_DOCTYPE);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.size(limitResults);
        searchSourceBuilder.storedFields(Arrays.asList(ARTIST, TITLE));
        searchSourceBuilder.fetchSource(true);
        BoolQueryBuilder filterQuery = QueryBuilders.boolQuery()
        		.filter(QueryBuilders.termQuery("isNewRelease", true))
        		.filter(QueryBuilders.rangeQuery("year")
        				.gte(year)
        				.lte(year)
        		);
        searchSourceBuilder.query(filterQuery);
        searchRequest.source(searchSourceBuilder);
        return searchRequest;
	}

}
