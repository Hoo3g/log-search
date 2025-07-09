package com.defi.search;

import com.defi.common.api.BaseResponse;
import com.defi.common.api.CommonError;
import com.defi.common.util.json.JsonUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.vertx.core.Future;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SearchManager {
    @Getter
    private static final SearchManager instance = new SearchManager();

    private SearchManager() {}

    /**
     * Tìm kiếm theo ID
     */
    public Future<BaseResponse<?>> searchById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return Future.succeededFuture(BaseResponse.of(CommonError.INVALID_PARAM, "ID cannot be null or empty"));
        }

        return SearchQuerier.getInstance().searchById(id)
                .compose(result -> Future.succeededFuture(BaseResponse.of(CommonError.SUCCESS, result)));
    }

    /**
     * Tìm kiếm theo target
     */
    public Future<BaseResponse<?>> searchByTarget(String targetType, String targetId) {
        if (targetType == null || targetId == null || targetType.trim().isEmpty() || targetId.trim().isEmpty()) {
            return Future.succeededFuture(BaseResponse.of(CommonError.INVALID_PARAM, "TargetType and targetId cannot be null or empty"));
        }

        return SearchQuerier.getInstance().searchByTarget(targetType, targetId)
                .compose(result -> {
                    return Future.succeededFuture(BaseResponse.of(CommonError.SUCCESS, result));
                });
    }

    /**
     * Tìm kiếm theo subject
     */
    public Future<BaseResponse<?>> searchBySubject(String subjectType, String subjectId) {
        if (subjectType == null || subjectId == null || subjectType.trim().isEmpty() || subjectId.trim().isEmpty()) {
            return Future.succeededFuture(BaseResponse.of(CommonError.INVALID_PARAM, "SubjectType and subjectId cannot be null or empty"));
        }

        return SearchQuerier.getInstance().searchBySubject(subjectType, subjectId)
                .compose(result -> {
                    return Future.succeededFuture(BaseResponse.of(CommonError.SUCCESS, result));
                });
    }

    /**
     * Tìm kiếm theo event type
     */
    public Future<BaseResponse<?>> searchByEventType(String eventType) {
        if (eventType == null || eventType.trim().isEmpty()) {
            return Future.succeededFuture(BaseResponse.of(CommonError.INVALID_PARAM, "EventType cannot be null or empty"));
        }

        return SearchQuerier.getInstance().searchByEventType(eventType)
                .compose(result -> {
                    return Future.succeededFuture(BaseResponse.of(CommonError.SUCCESS, result));
                });
    }

    /**
     * Tìm kiếm theo correlation ID
     */
    public Future<BaseResponse<?>> searchByCorrelationId(String correlationId) {
        if (correlationId == null || correlationId.trim().isEmpty()) {
            return Future.succeededFuture(BaseResponse.of(CommonError.INVALID_PARAM, "CorrelationId cannot be null or empty"));
        }

        return SearchQuerier.getInstance().searchByCorrelationId(correlationId)
                .compose(result -> {
                    return Future.succeededFuture(BaseResponse.of(CommonError.SUCCESS, result));
                });
    }

    /**
     * Tìm kiếm theo khoảng thời gian
     */
    public Future<BaseResponse<?>> searchByTimeRange(Long fromTime, Long toTime) {
        if (fromTime == null || toTime == null || fromTime > toTime) {
            return Future.succeededFuture(BaseResponse.of(CommonError.INVALID_PARAM, "Invalid time range parameters"));
        }

        return SearchQuerier.getInstance().searchByTimeRange(fromTime, toTime)
                .compose(result -> {
                    return Future.succeededFuture(BaseResponse.of(CommonError.SUCCESS, result));
                });
    }

    /**
     * Tìm kiếm trong data field
     */
    public Future<BaseResponse<?>> searchInData(String dataField, String value) {
        if (dataField == null || value == null || dataField.trim().isEmpty() || value.trim().isEmpty()) {
            return Future.succeededFuture(BaseResponse.of(CommonError.INVALID_PARAM, "DataField and value cannot be null or empty"));
        }

        return SearchQuerier.getInstance().searchInData(dataField, value)
                .compose(result -> {
                    return Future.succeededFuture(BaseResponse.of(CommonError.SUCCESS, result));
                });
    }

    /**
     * Tìm kiếm kết hợp nhiều điều kiện
     */
    public Future<BaseResponse<?>> searchMultipleConditions(String targetType, String eventType, Long fromTime, Long toTime) {
        if (fromTime != null && toTime != null && fromTime > toTime) {
            return Future.succeededFuture(BaseResponse.of(CommonError.INVALID_PARAM, "Invalid time range parameters"));
        }

        return SearchQuerier.getInstance().searchMultipleConditions(targetType, eventType, fromTime, toTime)
                .compose(result -> {
                    return Future.succeededFuture(BaseResponse.of(CommonError.SUCCESS, result));
                });
    }

    /**
     * Tìm kiếm logs gần đây
     */
    public Future<BaseResponse<?>> searchRecentLogs(int size) {
        if (size <= 0 || size > 1000) {
            return Future.succeededFuture(BaseResponse.of(CommonError.INVALID_PARAM, "Size must be between 1 and 1000"));
        }

        return SearchQuerier.getInstance().searchRecentLogs(size)
                .compose(result -> Future.succeededFuture(BaseResponse.of(CommonError.SUCCESS, result)));
    }

    /**
     * Tìm kiếm full-text
     */
    public Future<BaseResponse<?>> searchFullText(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            return Future.succeededFuture(BaseResponse.of(CommonError.INVALID_PARAM, "Search text cannot be null or empty"));
        }

        return SearchQuerier.getInstance().searchFullText(searchText)
                .compose(result -> Future.succeededFuture(BaseResponse.of(CommonError.SUCCESS, result)));
    }

    /**
     * Thống kê event types
     */
    public Future<BaseResponse<?>> getEventTypeStats() {
        return SearchQuerier.getInstance().getEventTypeStats()
                .compose(result -> Future.succeededFuture(BaseResponse.of(CommonError.SUCCESS, result)));
    }

    /**
     * Phương thức chung để parse parameters từ request
     */
    public Future<BaseResponse<?>> searchWithParams(ObjectNode params) {
        if (params == null) {
            return Future.succeededFuture(BaseResponse.of(CommonError.INVALID_PARAM, "Parameters cannot be null"));
        }

        JsonNode searchType = params.get("searchType");
        if (searchType == null || !searchType.isTextual()) {
            return Future.succeededFuture(BaseResponse.of(CommonError.INVALID_PARAM, "Missing or invalid searchType parameter"));
        }

        String type = searchType.asText();

        return switch (type) {
            case "byId" -> searchById(getStringParam(params, "id"));
            case "byTarget" -> searchByTarget(
                    getStringParam(params, "targetType"),
                    getStringParam(params, "targetId")
            );
            case "bySubject" -> searchBySubject(
                    getStringParam(params, "subjectType"),
                    getStringParam(params, "subjectId")
            );
            case "byEventType" -> searchByEventType(getStringParam(params, "eventType"));
            case "byCorrelationId" -> searchByCorrelationId(getStringParam(params, "correlationId"));
            case "byTimeRange" -> searchByTimeRange(
                    getLongParam(params, "fromTime"),
                    getLongParam(params, "toTime")
            );
            case "inData" -> searchInData(
                    getStringParam(params, "dataField"),
                    getStringParam(params, "value")
            );
            case "multipleConditions" -> searchMultipleConditions(
                    getStringParam(params, "targetType"),
                    getStringParam(params, "eventType"),
                    getLongParam(params, "fromTime"),
                    getLongParam(params, "toTime")
            );
            case "recent" -> {
                int size = getIntParam(params, "size", 10);
                yield searchRecentLogs(size);
            }
            case "fullText" -> searchFullText(getStringParam(params, "searchText"));
            case "eventTypeStats" -> getEventTypeStats();
            default ->
                    Future.succeededFuture(BaseResponse.of(CommonError.INVALID_PARAM, "Unknown searchType: " + type));
        };
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