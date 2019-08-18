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
package info.rsdev.playlists.dao

import info.rsdev.playlists.domain.ChartsItem
import info.rsdev.playlists.domain.Song
import info.rsdev.playlists.services.MusicChart
import org.elasticsearch.action.bulk.BulkProcessor
import org.elasticsearch.action.bulk.BulkRequest
import org.elasticsearch.action.bulk.BulkResponse
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.action.update.UpdateRequest
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.client.indices.CreateIndexRequest
import org.elasticsearch.client.indices.CreateIndexResponse
import org.elasticsearch.client.indices.GetIndexRequest
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.common.unit.ByteSizeUnit
import org.elasticsearch.common.unit.ByteSizeValue
import org.elasticsearch.common.unit.TimeValue
import org.elasticsearch.common.xcontent.XContentBuilder
import org.elasticsearch.common.xcontent.XContentFactory
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.aggregations.AggregationBuilders
import org.elasticsearch.search.aggregations.metrics.ParsedMax
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.slf4j.LoggerFactory
import java.io.IOException
import java.util.*
import javax.inject.Inject

class ElasticBulkWritingChartsItemDao @Inject
constructor(private val elasticsearchClient: RestHighLevelClient) : ChartsItemDao {

    private val bulkProcessor: BulkProcessor

    /* This class contains callback methods that are called prior to sending a bulk request and on the bulk response,
     * success or failure.
     */
    private val listener = object : BulkProcessor.Listener {

        override fun beforeBulk(executionId: Long, request: BulkRequest) {

        }

        override fun afterBulk(executionId: Long, request: BulkRequest, response: BulkResponse) {

        }

        override fun afterBulk(executionId: Long, request: BulkRequest, failure: Throwable) {

        }
    }

    /**
     * Define the JSON to create the chartsitem Doctype in ElasticSearch
     */
    private val chartsItemMapping: XContentBuilder
        get() {
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
                    .endObject()
        }

    init {
        this.bulkProcessor = BulkProcessor.builder({ bulkRequest, listener -> elasticsearchClient.bulkAsync(bulkRequest, RequestOptions.DEFAULT, listener) }, listener)
                .setBulkActions(-1)
                .setBulkSize(ByteSizeValue(5L, ByteSizeUnit.MB))
                .setConcurrentRequests(3)
                .setFlushInterval(TimeValue.timeValueSeconds(10L))
                .build()
    }

    override fun setupStoreWhenNeeded(): Boolean {
        if (!doesChartsItemIndexExist()) {
            createChartsItemIndex()
            return true
        }
        return false
    }

    private fun doesChartsItemIndexExist(): Boolean {
        try {
            val indexRequest = GetIndexRequest(CHARTSITEM_INDEX_NAME)
            return elasticsearchClient.indices().exists(indexRequest, RequestOptions.DEFAULT)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

    }

    private fun createChartsItemIndex() {
        val request = CreateIndexRequest(CHARTSITEM_INDEX_NAME)
        request.settings(Settings.builder()
                .put("index.number_of_shards", 1)
                .put("index.number_of_replicas", 0))

        request.mapping(chartsItemMapping)

        val response: CreateIndexResponse
        try {
            response = elasticsearchClient.indices().create(request, RequestOptions.DEFAULT)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

        if (!response.isAcknowledged) {
            throw RuntimeException("Creation of index '$CHARTSITEM_INDEX_NAME' is not acknowledged")
        }
        LOGGER.info("Created elasticsearch index $CHARTSITEM_INDEX_NAME")
    }

    override fun saveOrUpdate(chartsItem: ChartsItem) {
        val request = UpdateRequest(CHARTSITEM_INDEX_NAME, UUID.randomUUID().toString())
        request.docAsUpsert(true)
        request.doc(toContentBuilder(chartsItem))
        this.bulkProcessor.add(request)
    }

    private fun toContentBuilder(chartsItem: ChartsItem): XContentBuilder {
        return XContentFactory.jsonBuilder()
                .startObject()
                .field(CHARTNAME, chartsItem.chartName)
                .field(YEAR, chartsItem.year)
                .field(WEEK, chartsItem.weekNumber)
                .field(POSITION, chartsItem.position)
                .field(IS_NEW_IN_CHART, chartsItem.isNewRelease)
                .field(ARTIST, chartsItem.song.artist)
                .field(TITLE, chartsItem.song.title)
                .endObject()
    }

    fun close() {
        this.bulkProcessor.close()
    }

    override fun getReleases(year: Short): Collection<Song> {
        val expectedResults = getReleasesCount(year, ROUNDUP_FACTOR)
        if (expectedResults == 0) {
            return emptyList()
        }

        val searchRequest = makeSearchRequestReleasesByYear(year, expectedResults)
        val searchResponse = elasticsearchClient.search(searchRequest, RequestOptions.DEFAULT)
        val releases = LinkedHashSet<Song>()
        searchResponse.hits.forEach { searchHit -> releases.add(makeSong(searchHit.sourceAsMap)) }
        return releases
    }

    private fun makeSong(properties: Map<String, Any>): Song {
        val artist = properties[ARTIST] as String
        val title = properties[TITLE] as String
        return Song(artist, title)
    }

    private fun getReleasesCount(year: Short, roundFactor: Int): Int {
        val searchRequest = makeSearchRequestReleasesByYear(year, RETURN_NO_DOCUMENTS)
        try {
            val searchResponse = elasticsearchClient.search(searchRequest, RequestOptions.DEFAULT)
            var elasticSearchResultCount = searchResponse.hits.totalHits.value  //TODO:reinterpret -> could be more than...
            if (elasticSearchResultCount == 0L) {
                return 0
            }
            elasticSearchResultCount = (elasticSearchResultCount / roundFactor + 1) * roundFactor
            if (elasticSearchResultCount > Integer.MAX_VALUE) {
                throw IllegalStateException("totalhits > Integer.MAX_VALUE is not supported")
            }
            return elasticSearchResultCount.toInt()
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

    }

    private fun makeSearchRequestReleasesByYear(year: Short, limitResults: Int): SearchRequest {
        val searchRequest = SearchRequest(CHARTSITEM_INDEX_NAME)
        val searchSourceBuilder = SearchSourceBuilder()
        searchSourceBuilder.size(limitResults)
        searchSourceBuilder.storedFields(Arrays.asList(ARTIST, TITLE))
        searchSourceBuilder.fetchSource(true)
        val filterQuery = QueryBuilders.boolQuery()
                .filter(QueryBuilders.termQuery(IS_NEW_IN_CHART, true))
                .filter(QueryBuilders.rangeQuery(YEAR)
                        .gte(year)
                        .lte(year)
                )
        searchSourceBuilder.query(filterQuery)
        searchRequest.source(searchSourceBuilder)
        return searchRequest
    }

    override fun getHighestYearStored(chart: MusicChart): Short {
        val searchRequest = SearchRequest(CHARTSITEM_INDEX_NAME)
        val searchSourceBuilder = SearchSourceBuilder()
        searchSourceBuilder.storedFields(Arrays.asList(CHARTNAME, YEAR))
        searchSourceBuilder.size(0)
        searchSourceBuilder.fetchSource(false)
        val filterQuery = QueryBuilders.boolQuery()
                .filter(QueryBuilders.termQuery(CHARTNAME, chart.chartName))
        searchSourceBuilder.query(filterQuery)

        searchSourceBuilder.aggregation(AggregationBuilders.max("maxYear").field(YEAR))
        searchRequest.source(searchSourceBuilder)

        try {
            val searchResponse = elasticsearchClient.search(searchRequest, RequestOptions.DEFAULT)
            val result = searchResponse.aggregations.get<ParsedMax>("maxYear")
            return if (java.lang.Double.isInfinite(result.value)) {
                -1
            } else {
                result.value.toShort()
            }
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

    }

    override fun getHighestWeekStored(chart: MusicChart, year: Short): Byte {
        val searchRequest = SearchRequest(CHARTSITEM_INDEX_NAME)
        val searchSourceBuilder = SearchSourceBuilder()
        searchSourceBuilder.storedFields(Arrays.asList(CHARTNAME, YEAR, WEEK))
        searchSourceBuilder.size(0)
        searchSourceBuilder.fetchSource(false)
        val filterQuery = QueryBuilders.boolQuery()
                .filter(QueryBuilders.termQuery(CHARTNAME, chart.chartName))
                .filter(QueryBuilders.rangeQuery(YEAR)
                        .gte(year)
                        .lte(year)
                )
        searchSourceBuilder.query(filterQuery)

        searchSourceBuilder.aggregation(AggregationBuilders.max("maxWeek").field(WEEK))
        searchRequest.source(searchSourceBuilder)

        try {
            val searchResponse = elasticsearchClient.search(searchRequest, RequestOptions.DEFAULT)
            val result = searchResponse.aggregations.get<ParsedMax>("maxWeek")
            return if (java.lang.Double.isInfinite(result.value)) {
                -1
            } else {
                result.value.toByte()
            }
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

    }

    companion object {

        private val LOGGER = LoggerFactory.getLogger(ElasticBulkWritingChartsItemDao::class.java)

        private val CHARTSITEM_INDEX_NAME = "chartitems"
        private val CHARTSITEM_DOCTYPE = "chartitem"

        private val CHARTNAME = "chartName"
        private val YEAR = "year"
        private val WEEK = "weekNumber"
        private val POSITION = "position"
        private val IS_NEW_IN_CHART = "isNewRelease"
        private val ARTIST = "artist"
        private val TITLE = "title"


        private val RETURN_NO_DOCUMENTS = 0
        private val ROUNDUP_FACTOR = 100
    }

}
