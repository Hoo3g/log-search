package com.defi.search;

import com.defi.common.util.json.JsonUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.vertx.core.Future;
import lombok.Getter;

public class SearchManager {
    @Getter
    private static final SearchManager instance = new SearchManager();

    private SearchManager() {}


    /**
     * Tìm kiếm theo ID
     */
    public Future<ObjectNode> searchById(String id) {
        return SearchQuerier.getInstance().searchById(id);
    }

    /**
     * Tìm kiếm theo target
     */
    public Future<ObjectNode> searchByTarget(String targetType, String targetId) {
        return SearchQuerier.getInstance().searchByTarget(targetType, targetId);
    }

    /**
     * Tìm kiếm theo subject
     */
    public Future<ObjectNode> searchBySubject(String subjectType, String subjectId) {
        return SearchQuerier.getInstance().searchBySubject(subjectType, subjectId);
    }

    /**
     * Tìm kiếm theo event type
     */
    public Future<ObjectNode> searchByEventType(String eventType) {
        return SearchQuerier.getInstance().searchByEventType(eventType);
    }

    /**
     * Tìm kiếm theo correlation ID
     */
    public Future<ObjectNode> searchByCorrelationId(String correlationId) {
        return SearchQuerier.getInstance().searchByCorrelationId(correlationId);
    }

    /**
     * Tìm kiếm theo khoảng thời gian
     */
    public Future<ObjectNode> searchByTimeRange(Long fromTime, Long toTime) {
        return SearchQuerier.getInstance().searchByTimeRange(fromTime, toTime);
    }

    /**
     * Tìm kiếm trong data field
     */
    public Future<ObjectNode> searchInData(String dataField, String value) {
        return SearchQuerier.getInstance().searchInData(dataField, value);
    }

    /**
     * Tìm kiếm kết hợp nhiều điều kiện
     */
    public Future<ObjectNode> searchMultipleConditions(String targetType, String eventType, Long fromTime, Long toTime) {
        return SearchQuerier.getInstance().searchMultipleConditions(targetType, eventType, fromTime, toTime);
    }

    /**
     * Tìm kiếm logs gần đây
     */
    public Future<ObjectNode> searchRecentLogs(int size) {
        return SearchQuerier.getInstance().searchRecentLogs(size);
    }

    /**
     * Tìm kiếm full-text
     */
    public Future<ObjectNode> searchFullText(String searchText) {
        return SearchQuerier.getInstance().searchFullText(searchText);
    }

    /**
     * Thống kê event types
     */
    public Future<ObjectNode> getEventTypeStats() {
        return SearchQuerier.getInstance().getEventTypeStats();
    }

    /**
     * Phương thức chung để parse parameters từ request
     */
    public Future<ObjectNode> searchWithParams(ObjectNode params) {
        JsonNode searchType = params.get("searchType");
        if (searchType == null || !searchType.isTextual()) {
            return Future.failedFuture("Missing or invalid searchType parameter");
        }

        String type = searchType.asText();

        switch (type) {
            case "byId":
                return searchById(getStringParam(params, "id"));

            case "byTarget":
                return searchByTarget(
                        getStringParam(params, "targetType"),
                        getStringParam(params, "targetId")
                );

            case "bySubject":
                return searchBySubject(
                        getStringParam(params, "subjectType"),
                        getStringParam(params, "subjectId")
                );

            case "byEventType":
                return searchByEventType(getStringParam(params, "eventType"));

            case "byCorrelationId":
                return searchByCorrelationId(getStringParam(params, "correlationId"));

            case "byTimeRange":
                return searchByTimeRange(
                        getLongParam(params, "fromTime"),
                        getLongParam(params, "toTime")
                );

            case "inData":
                return searchInData(
                        getStringParam(params, "dataField"),
                        getStringParam(params, "value")
                );

            case "multipleConditions":
                return searchMultipleConditions(
                        getStringParam(params, "targetType"),
                        getStringParam(params, "eventType"),
                        getLongParam(params, "fromTime"),
                        getLongParam(params, "toTime")
                );

            case "recent":
                int size = getIntParam(params, "size", 10);
                return searchRecentLogs(size);

            case "fullText":
                return searchFullText(getStringParam(params, "searchText"));

            case "eventTypeStats":
                return getEventTypeStats();

            default:
                return Future.failedFuture("Unknown searchType: " + type);
        }
    }

    // Helper methods
    private String getStringParam(ObjectNode params, String key) {
        JsonNode node = params.get(key);
        return node != null && node.isTextual() ? node.asText() : null;
    }

    private Long getLongParam(ObjectNode params, String key) {
        JsonNode node = params.get(key);
        return node != null && node.isNumber() ? node.asLong() : null;
    }

    private int getIntParam(ObjectNode params, String key, int defaultValue) {
        JsonNode node = params.get(key);
        return node != null && node.isNumber() ? node.asInt() : defaultValue;
    }
}