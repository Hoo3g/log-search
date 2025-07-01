package com.defi.search;

import com.defi.common.util.json.JsonUtil;
import com.defi.search.config.SearchConfig;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.vertx.core.Future;
import io.vertx.core.Promise;
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
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.search.Hit;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch._types.SortOrder;
import org.opensearch.client.transport.rest_client.RestClientTransport;

import com.fasterxml.jackson.databind.node.ArrayNode; // <<-- THÊM IMPORT NÀY
import java.util.stream.Collectors; // <<-- THÊM IMPORT NÀY
import org.opensearch.client.opensearch.core.search.Hit;

import java.util.List;
import java.util.Map;

@Slf4j
public class SearchQuerier {
    @Getter
    private static final SearchQuerier instance = new SearchQuerier();

    private OpenSearchAsyncClient asyncClient;
    private String indexName;

    private SearchQuerier() {}

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

            log.info("OpenSearch AsyncClient for querying initialized successfully. Index target: {}", this.indexName);
        } catch (Exception e) {
            log.error("Failed to initialize OpenSearch AsyncClient", e);
            throw new RuntimeException("Could not initialize SearchQuerier", e);
        }
    }

    /**
     * Tìm kiếm theo ID cụ thể
     */
    public Future<ObjectNode> searchById(String id) {
        return executeSearch(Query.of(q -> q
                .term(t -> t
                        .field("id")
                        .value(FieldValue.of(id))
                )
        ));
    }

    /**
     * Tìm kiếm theo targetType và targetId
     */
    public Future<ObjectNode> searchByTarget(String targetType, String targetId) {
        return executeSearch(Query.of(q -> q
                .bool(b -> b
                        .must(m -> m.term(t -> t.field("targetType").value(FieldValue.of(targetType))))
                        .must(m -> m.term(t -> t.field("targetId").value(FieldValue.of(targetId))))
                )
        ));
    }

    /**
     * Tìm kiếm theo subjectType và subjectId
     */
    public Future<ObjectNode> searchBySubject(String subjectType, String subjectId) {
        return executeSearch(Query.of(q -> q
                .bool(b -> b
                        .must(m -> m.term(t -> t.field("subjectType").value(FieldValue.of(subjectType))))
                        .must(m -> m.term(t -> t.field("subjectId").value(FieldValue.of(subjectId))))
                )
        ));
    }

    /**
     * Tìm kiếm theo type (event type)
     */
    public Future<ObjectNode> searchByEventType(String eventType) {
        return executeSearch(Query.of(q -> q
                .term(t -> t
                        .field("type")
                        .value(FieldValue.of(eventType))
                )
        ));
    }

    /**
     * Tìm kiếm theo correlationId
     */
    public Future<ObjectNode> searchByCorrelationId(String correlationId) {
        return executeSearch(Query.of(q -> q
                .term(t -> t
                        .field("correlationId")
                        .value(FieldValue.of(correlationId))
                )
        ));
    }

    /**
     * Tìm kiếm theo khoảng thời gian
     */
    public Future<ObjectNode> searchByTimeRange(Long fromTime, Long toTime) {
        return executeSearch(Query.of(q -> q
                .range(r -> r
                        .field("createdAt")
                        .gte(JsonUtil.mapper.valueToTree(fromTime))
                        .lte(JsonUtil.mapper.valueToTree(toTime))
                )
        ));
    }

    /**
     * Tìm kiếm trong data field (nested search)
     */
    public Future<ObjectNode> searchInData(String dataField, String value) {
        return executeSearch(Query.of(q -> q
                .term(t -> t
                        .field("data." + dataField)
                        .value(FieldValue.of(value))
                )
        ));
    }

    /**
     * Tìm kiếm kết hợp nhiều điều kiện
     */
    public Future<ObjectNode> searchMultipleConditions(String targetType, String eventType, Long fromTime, Long toTime) {
        return executeSearch(Query.of(q -> q
                .bool(b -> {
                    if (targetType != null) {
                        b.must(m -> m.term(t -> t.field("targetType").value(FieldValue.of(targetType))));
                    }
                    if (eventType != null) {
                        b.must(m -> m.term(t -> t.field("type").value(FieldValue.of(eventType))));
                    }
                    if (fromTime != null && toTime != null) {
                        b.must(m -> m.range(r -> r
                                .field("createdAt")
                                .gte(JsonUtil.mapper.valueToTree(fromTime))
                                .lte(JsonUtil.mapper.valueToTree(toTime))
                        ));
                    }
                    return b;
                })
        ));
    }

    /**
     * Tìm kiếm logs gần đây nhất (sắp xếp theo createdAt desc)
     */
    public Future<ObjectNode> searchRecentLogs(int size) {
        Promise<ObjectNode> promise = Promise.promise();

        if (asyncClient == null) {
            return Future.failedFuture("SearchQuerier is not initialized.");
        }

        try {
            SearchRequest searchRequest = new SearchRequest.Builder()
                    .index(this.indexName)
                    .query(Query.of(q -> q.matchAll(m -> m)))
                    .sort(s -> s.field(f -> f.field("createdAt").order(SortOrder.Desc)))
                    .size(size)
                    .build();

            asyncClient.search(searchRequest, ObjectNode.class)
                    .whenComplete((response, exception) -> {
                        if (exception != null) {
                            log.error("OpenSearch query failed", exception);
                            promise.fail(exception);
                        } else {
                            promise.complete(buildResponse(response.hits().hits(), response.hits().total().value(),
                                    response.timedOut(), response.took()));
                        }
                    });

        } catch (Exception e) {
            log.error("Failed to build search request", e);
            promise.fail(e);
        }

        return promise.future();
    }

    /**
     * Tìm kiếm full-text trong tất cả các field text
     */
    public Future<ObjectNode> searchFullText(String searchText) {
        return executeSearch(Query.of(q -> q
                .multiMatch(m -> m
                        .query(searchText)
                        .fields("targetType", "targetId", "subjectId", "type", "correlationId")
                )
        ));
    }

    /**
     * Aggregation - Thống kê theo event type
     */
    public Future<ObjectNode> getEventTypeStats() {
        Promise<ObjectNode> promise = Promise.promise();

        if (asyncClient == null) {
            return Future.failedFuture("SearchQuerier is not initialized.");
        }

        try {
            SearchRequest searchRequest = new SearchRequest.Builder()
                    .index(this.indexName)
                    .size(0) // Không cần hits, chỉ cần aggregation
                    .aggregations("event_types", a -> a
                            .terms(t -> t.field("type").size(100))
                    )
                    .build();

            asyncClient.search(searchRequest, ObjectNode.class)
                    .whenComplete((response, exception) -> {
                        if (exception != null) {
                            log.error("OpenSearch aggregation query failed", exception);
                            promise.fail(exception);                        } else {
                            ObjectNode result = JsonUtil.createObjectNode();
                            result.putPOJO("aggregations", response.aggregations());
                            promise.complete(result);
                        }
                    });

        } catch (Exception e) {
            log.error("Failed to build aggregation request", e);
            promise.fail(e);
        }

        return promise.future();
    }

    /**
     * Phương thức helper để thực thi search với query cho trước
     */
    private Future<ObjectNode> executeSearch(Query query) {
        return executeSearch(query, 100); // Default size 100
    }

    private Future<ObjectNode> executeSearch(Query query, int size) {
        Promise<ObjectNode> promise = Promise.promise();

        if (asyncClient == null) {
            return Future.failedFuture("SearchQuerier is not initialized.");
        }

        try {
            SearchRequest searchRequest = new SearchRequest.Builder()
                    .index(this.indexName)
                    .query(query)
                    .size(size)
                    .sort(s -> s.field(f -> f.field("createdAt").order(SortOrder.Desc)))
                    .build();

            asyncClient.search(searchRequest, ObjectNode.class)
                    .whenComplete((response, exception) -> {
                        if (exception != null) {
                            log.error("OpenSearch query failed", exception);
                            promise.fail(exception);
                        } else {
                            assert response.hits().total() != null;
                            promise.complete(buildResponse(response.hits().hits(), response.hits().total().value(),
                                    response.timedOut(), response.took()));
                        }
                    });

        } catch (Exception e) {
            log.error("Failed to build search request", e);
            promise.fail(e);
        }

        return promise.future();
    }

    /**
     * Helper method để build response
     */
    private ObjectNode buildResponse(List<Hit<ObjectNode>> hits, long total, boolean timedOut, long took) {
        ObjectNode result = JsonUtil.createObjectNode();

        // Extract chỉ phần source của mỗi hit
        ArrayNode sources = JsonUtil.createArrayNode();
        hits.forEach(hit -> {
            ObjectNode source = hit.source();  // chính là document JSON
            if (source != null) {
                sources.add(source);
            }
        });

        result.set("hits", sources);
        result.put("total", total);
        result.put("timed_out", timedOut);
        result.put("took", took);
        return result;
    }

}