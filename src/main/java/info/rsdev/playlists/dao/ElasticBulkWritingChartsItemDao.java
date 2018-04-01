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
import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

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
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.rsdev.playlists.domain.ChartsItem;

public class ElasticBulkWritingChartsItemDao implements ChartsItemDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticBulkWritingChartsItemDao.class);

    private static final String CHARTSITEM_INDEX_NAME = "chartitems";
    private static final String CHARTSITEM_DOCTYPE = "chartitem";

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
                            .startObject("chartName").field("type", "keyword").endObject()
                            .startObject("year").field("type", "short").endObject()
                            .startObject("weekNumber").field("type", "byte").endObject()
                            .startObject("position").field("type", "byte").endObject()
                            .startObject("isNewRelease").field("type", "boolean").endObject()
                            .startObject("artist").field("type", "text").endObject()
                            .startObject("title").field("type", "text").endObject()
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

    public Collection<String> getAllPlaatsnamen() {
        int estimatedCardinality = countUniqueTerms("woonplaats", 1000);
        return getUniqueTerms("woonplaats", estimatedCardinality);
    }

    public Collection<String> getAllGemeentenamen() {
        int estimatedCardinality = countUniqueTerms("gemeente", 1000);
        return getUniqueTerms("gemeente", estimatedCardinality);
    }

    private int countUniqueTerms(String fieldName, int roundFactor) {
        //TODO: ask ES for the right number and roundup to nearest multiple of roundFactor
        int elasticSearchResultCount = 2421;    //fictional number
        return ((elasticSearchResultCount / roundFactor) + 1) * roundFactor;
    }

    private Collection<String> getUniqueTerms(String fieldName, int expectedSize) {
        SearchRequest searchRequest = new SearchRequest(CHARTSITEM_INDEX_NAME);
        searchRequest.types(CHARTSITEM_DOCTYPE);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.fetchSource(false);
        TermsAggregationBuilder aggregation = AggregationBuilders.terms("by_field")
                .field(fieldName)
                .size(expectedSize);
        searchSourceBuilder.aggregation(aggregation);
        searchRequest.source(searchSourceBuilder);
        try {
            SearchResponse searchResponse = elasticsearchClient.search(searchRequest);
            Terms plaatsnamen = searchResponse.getAggregations().get("by_field");
            return plaatsnamen.getBuckets().stream()
                    .map(bucket -> bucket.getKeyAsString())
                    .collect(Collectors.toSet());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private XContentBuilder toContentBuilder(ChartsItem chartsItem) {
        try {
            return XContentFactory.jsonBuilder()
                .startObject()
                    .field("chartName", chartsItem.chartName)
                    .field("year", chartsItem.year)
                    .field("weekNumber", chartsItem.weekNumber)
                    .field("position", chartsItem.position)
                    .field("isNewRelease", chartsItem.isNewRelease)
                    .field("artist", chartsItem.artist)
                    .field("title", chartsItem.title)
                .endObject();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void close() {
        this.bulkProcessor.close();
    }

}
