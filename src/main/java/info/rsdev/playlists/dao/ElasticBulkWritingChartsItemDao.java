package info.rsdev.playlists.dao;

import info.rsdev.playlists.domain.ChartsItem;
import info.rsdev.playlists.domain.Song;
import info.rsdev.playlists.services.MusicChart;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.metrics.ParsedMax;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

public class ElasticBulkWritingChartsItemDao implements ChartsItemDao, Initializable {

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


    private RestHighLevelClient elasticsearchClient;

    private BulkProcessor bulkProcessor;

    /* This class contains callback methods that are called prior to sending a bulk request and on the bulk response,
     * success or failure.
     */
    private BulkProcessor.Listener listener = new BulkProcessor.Listener() {

        @Override
        public void beforeBulk(long executionId, BulkRequest request) {
            //no implementation
        }

        @Override
        public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {
            //no implementation
        }

        @Override
        public void afterBulk(long executionId, BulkRequest request, Throwable failure) {
            //no implementation
        }
    };


    public ElasticBulkWritingChartsItemDao(RestHighLevelClient elasticsearchClient) {
        this.elasticsearchClient = elasticsearchClient;
        this.bulkProcessor = BulkProcessor.builder((bulkRequest, listener) -> this.elasticsearchClient.bulkAsync(bulkRequest, RequestOptions.DEFAULT, listener), listener)
                .setBulkActions(-1)
                .setBulkSize(new ByteSizeValue(5L, ByteSizeUnit.MB))
                .setConcurrentRequests(3)
                .setFlushInterval(TimeValue.timeValueSeconds(10L))
                .build();
    }

    /**
     * Define the JSON to create the chartsitem Doctype in ElasticSearch
     */
    private XContentBuilder getChartsItemMapping() throws IOException {
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
        try {
            var indexRequest = new GetIndexRequest(CHARTSITEM_INDEX_NAME);
            return elasticsearchClient.indices().exists(indexRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void createChartsItemIndex() {
        var request = new CreateIndexRequest(CHARTSITEM_INDEX_NAME);
        request.settings(Settings.builder()
                .put("index.number_of_shards", 1)
                .put("index.number_of_replicas", 0));


        CreateIndexResponse response;
        try {
            request.mapping(getChartsItemMapping());
            response = elasticsearchClient.indices().create(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (!response.isAcknowledged()) {
            throw new RuntimeException("Creation of index '"+CHARTSITEM_INDEX_NAME+"' is not acknowledged");
        }
        LOGGER.info("Created elasticsearch index {}", CHARTSITEM_INDEX_NAME);
    }

    @Override
    public void insert(ChartsItem chartsItem) {
        var request = new UpdateRequest(CHARTSITEM_INDEX_NAME, UUID.randomUUID().toString());
        request.docAsUpsert(true);
        request.doc(toContentBuilder(chartsItem));
        this.bulkProcessor.add(request);
    }

    @Override
    public void insert(List<ChartsItem> chartsItems) {
        if (chartsItems != null) {
            chartsItems.forEach(this::insert);
        }
    }
    
    private XContentBuilder toContentBuilder(ChartsItem chartsItem) {
        try {
            return XContentFactory.jsonBuilder()
                    .startObject()
                    .field(CHARTNAME, chartsItem.chartName())
                    .field(YEAR, chartsItem.year())
                    .field(WEEK, chartsItem.weekNumber())
                    .field(POSITION, chartsItem.position())
                    .field(IS_NEW_IN_CHART, chartsItem.isNewRelease())
                    .field(ARTIST, chartsItem.song().artist())
                    .field(TITLE, chartsItem.song().title())
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
        var expectedResults = getReleasesCount(year, ROUNDUP_FACTOR);
        if (expectedResults == 0) {
            return Collections.emptyList();
        }

        var searchRequest = makeSearchRequestReleasesByYear(year, expectedResults);
        try {
            var searchResponse = elasticsearchClient.search(searchRequest, RequestOptions.DEFAULT);
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
        var searchRequest = makeSearchRequestReleasesByYear(year, RETURN_NO_DOCUMENTS);
        try {
            var searchResponse = elasticsearchClient.search(searchRequest, RequestOptions.DEFAULT);
            var elasticSearchResultCount = searchResponse.getHits().getTotalHits().value;  //TODO:reinterpret -> could be more than...
            if (elasticSearchResultCount == 0L) {
                return 0;
            }
            elasticSearchResultCount = (elasticSearchResultCount / roundFactor + 1) * roundFactor;
            if (elasticSearchResultCount > Integer.MAX_VALUE) {
                throw new IllegalStateException("totalhits > Integer.MAX_VALUE is not supported");
            }
            return (int)elasticSearchResultCount;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private SearchRequest makeSearchRequestReleasesByYear(short year, int limitResults) {
        var searchRequest = new SearchRequest(CHARTSITEM_INDEX_NAME);
        var searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.size(limitResults);
        searchSourceBuilder.storedFields(Arrays.asList(ARTIST, TITLE));
        searchSourceBuilder.fetchSource(true);
        var filterQuery = QueryBuilders.boolQuery()
                .filter(QueryBuilders.termQuery(IS_NEW_IN_CHART, true))
                .filter(QueryBuilders.rangeQuery(YEAR)
                        .gte(year)
                        .lte(year)
                );
        searchSourceBuilder.query(filterQuery);
        searchRequest.source(searchSourceBuilder);
        return searchRequest;
    }

    @Override
    public short getHighestYearStored(MusicChart chart) {
        var searchRequest = new SearchRequest(CHARTSITEM_INDEX_NAME);
        var searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.storedFields(Arrays.asList(CHARTNAME, YEAR));
        searchSourceBuilder.size(0);
        searchSourceBuilder.fetchSource(false);
        var filterQuery = QueryBuilders.boolQuery()
                .filter(QueryBuilders.termQuery(CHARTNAME, chart.chartName()));
        searchSourceBuilder.query(filterQuery);

        searchSourceBuilder.aggregation(AggregationBuilders.max("maxYear").field(YEAR));
        searchRequest.source(searchSourceBuilder);

        try {
            var searchResponse = elasticsearchClient.search(searchRequest, RequestOptions.DEFAULT);
            var result = searchResponse.getAggregations().<ParsedMax>get("maxYear");
            return (short)(Double.isInfinite(result.value())? -1: result.value());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte getHighestWeekStored(MusicChart chart, short year) {
        var searchRequest = new SearchRequest(CHARTSITEM_INDEX_NAME);
        var searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.storedFields(Arrays.asList(CHARTNAME, YEAR, WEEK));
        searchSourceBuilder.size(0);
        searchSourceBuilder.fetchSource(false);
        var filterQuery = QueryBuilders.boolQuery()
                .filter(QueryBuilders.termQuery(CHARTNAME, chart.chartName()))
                .filter(QueryBuilders.rangeQuery(YEAR)
                        .gte(year)
                        .lte(year)
                );
        searchSourceBuilder.query(filterQuery);

        searchSourceBuilder.aggregation(AggregationBuilders.max("maxWeek").field(WEEK));
        searchRequest.source(searchSourceBuilder);

        try {
            var searchResponse = elasticsearchClient.search(searchRequest, RequestOptions.DEFAULT);
            var result = searchResponse.getAggregations().<ParsedMax>get("maxWeek");
            return (byte)(Double.isInfinite(result.value())? -1:result.value());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
