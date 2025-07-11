package com.defi.search.service.impl;

import com.defi.common.util.json.JsonUtil;
import com.defi.common.util.log.entity.EventLog;
import com.defi.search.config.SearchConfig;
import com.defi.search.dto.TargetCount;
import com.defi.search.dto.TypeCount;
import com.defi.search.dto.UserActivityCount;
import com.defi.search.service.SearchLog;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.opensearch.client.RestClient;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.opensearch.OpenSearchAsyncClient;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.aggregations.Aggregate;
import org.opensearch.client.opensearch._types.aggregations.TermsAggregation;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.search.Hit;
import org.opensearch.client.transport.rest_client.RestClientTransport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;


@Slf4j
public class SearchLogImpl implements SearchLog {
    private SearchLogImpl() {}
    @Getter
    private static final SearchLogImpl instance = new SearchLogImpl();
    private OpenSearchAsyncClient asyncClient;
    private String indexName;

    public void init() {
        try {
            log.info("Initializing SearchQuerier...");
            ObjectNode osConfig = (ObjectNode) SearchConfig.getInstance().getConfig().get("opensearch");
            String host = osConfig.get("host").asText();
            int port = osConfig.get("port").asInt();
            String scheme = osConfig.get("scheme").asText("http");
            String username = osConfig.get("username").asText(null);
            String password = osConfig.get("password").asText(null);
            this.indexName = osConfig.get("indexName").asText();

            final BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            if (username != null && !username.isEmpty()) {
                credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
            }

            RestClient restClient = RestClient.builder(new HttpHost(host, port, scheme))
                    .setHttpClientConfigCallback(httpClientBuilder ->
                            httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider))
                    .build();

            RestClientTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
            this.asyncClient = new OpenSearchAsyncClient(transport);

            log.info("OpenSearch AsyncClient initialized successfully. Index: {}", this.indexName);
        } catch (Exception e) {
            log.error("Failed to initialize OpenSearch AsyncClient", e);
            throw new RuntimeException("Could not initialize SearchQuerier", e);
        }
    }

    /**
     * Helper method to build a time range query.
     */
    private Query buildTimeRangeQuery(Long startTime, Long endTime) {
        return new Query.Builder()
                .range(r -> r
                        .field("createdAt")
                        .gte(JsonUtil.mapper.valueToTree(startTime))
                        .lte(JsonUtil.mapper.valueToTree(endTime))
                ).build();
    }

    /**
     * Helper method to execute a search and map results to a list of EventLog.
     */
    private List<EventLog> executeSearch(SearchRequest request) {
        try {
            // Since the client is async, we wait for the result.
            // In a fully async application, you might want to return the CompletableFuture itself.
            SearchResponse<EventLog> response = asyncClient.search(request, EventLog.class).get();
            return response.hits().hits().stream()
                    .map(Hit::source)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error executing OpenSearch query", e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<EventLog> findByDateRange(Long startTime, Long endTime) {
        SearchRequest request = new SearchRequest.Builder()
                .index(indexName)
                .query(buildTimeRangeQuery(startTime, endTime))
                .size(1000) // Add a reasonable size limit
                .build();
        return executeSearch(request);
    }

    @Override
    public List<EventLog> findUserByType(String type, Long startTime, Long endTime) {
        Query query = new Query.Builder()
                .bool(b -> b
                        .filter(buildTimeRangeQuery(startTime, endTime))
                        // Use .keyword for exact matching on text fields
                        .must(m -> m.term(t -> t.field("type.keyword").value(FieldValue.of(type))))
                ).build();

        SearchRequest request = new SearchRequest.Builder()
                .index(indexName)
                .query(query)
                .size(1000)
                .build();
        return executeSearch(request);
    }

    @Override
    public List<EventLog> findUserByTargetType(String targetType, Long startTime, Long endTime) {
        Query query = new Query.Builder()
                .bool(b -> b
                        .filter(buildTimeRangeQuery(startTime, endTime))
                        .must(m -> m.term(t -> t.field("targetType.keyword").value(FieldValue.of(targetType))))
                ).build();

        SearchRequest request = new SearchRequest.Builder()
                .index(indexName)
                .query(query)
                .size(1000)
                .build();
        return executeSearch(request);
    }

    @Override
    public List<EventLog> findUserBySubjectType(String subjectType, Long startTime, Long endTime) {
        Query query = new Query.Builder()
                .bool(b -> b
                        .filter(buildTimeRangeQuery(startTime, endTime))
                        .must(m -> m.term(t -> t.field("subjectType.keyword").value(FieldValue.of(subjectType))))
                ).build();

        SearchRequest request = new SearchRequest.Builder()
                .index(indexName)
                .query(query)
                .size(1000)
                .build();
        return executeSearch(request);
    }

    private <T> List<T> executeAggregation(SearchRequest request, String aggName, AggregationResultParser<T> parser) {
        try {
            CompletableFuture<SearchResponse<Void>> future = asyncClient.search(request, Void.class);
            SearchResponse<Void> response = future.get();

            Aggregate aggregate = response.aggregations().get(aggName);
            List<T> results = new ArrayList<>();
            aggregate.sterms().buckets().array().forEach(bucket -> {
                results.add(parser.parse(bucket));
            });
            return results;
        } catch (Exception e) {
            log.error("Error executing OpenSearch aggregation for '{}'", aggName, e);
            return Collections.emptyList();
        }
    }

    @FunctionalInterface
    interface AggregationResultParser<T> {
        T parse(org.opensearch.client.opensearch._types.aggregations.StringTermsBucket bucket);
    }

    @Override
    public List<TargetCount> countEventsByTarget(Long startTime, Long endTime, int size) {
        String aggName = "group_by_target";
        TermsAggregation aggregation = new TermsAggregation.Builder()
                .field("targetId.keyword")
                .size(size)
                .build();

        SearchRequest request = new SearchRequest.Builder()
                .index(indexName)
                .query(buildTimeRangeQuery(startTime, endTime))
                .size(0) // We don't need the documents, just the aggregation results
                .aggregations(aggName, agg -> agg.terms(aggregation))
                .build();

        return executeAggregation(request, aggName, bucket ->
                new TargetCount(bucket.key(), bucket.docCount()));
    }

    @Override
    public List<TypeCount> countEventsByType(Long startTime, Long endTime, int size) {
        String aggName = "group_by_type";
        TermsAggregation aggregation = new TermsAggregation.Builder()
                .field("type.keyword")
                .size(size)
                .build();

        SearchRequest request = new SearchRequest.Builder()
                .index(indexName)
                .query(buildTimeRangeQuery(startTime, endTime))
                .size(0)
                .aggregations(aggName, agg -> agg.terms(aggregation))
                .build();

        return executeAggregation(request, aggName, bucket ->
                new TypeCount(bucket.key(), bucket.docCount()));
    }

    @Override
    public List<UserActivityCount> findTopUsersByEventCount(Long startTime, Long endTime, int size) {
        String aggName = "top_users";
        TermsAggregation aggregation = new TermsAggregation.Builder()
                .field("subjectId.keyword")
                .size(size)
                .build();

        SearchRequest request = new SearchRequest.Builder()
                .index(indexName)
                .query(buildTimeRangeQuery(startTime, endTime))
                .size(0)
                .aggregations(aggName, agg -> agg.terms(aggregation))
                .build();

        return executeAggregation(request, aggName, bucket ->
                new UserActivityCount(bucket.key(), bucket.docCount()));
    }

    @Override
    public List<EventLog> findEventLogBySubjectId(String subjectId) {
        Query query = new Query.Builder()
                .term(t -> t.field("subjectId.keyword").value(FieldValue.of(subjectId)))
                .build();

        SearchRequest request = new SearchRequest.Builder()
                .index(indexName)
                .query(query)
                .size(1000)
                .build();
        return executeSearch(request);
    }
}
