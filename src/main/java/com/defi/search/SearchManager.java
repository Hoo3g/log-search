package com.defi.search;

import com.defi.common.api.BaseResponse;
import com.defi.common.api.CommonError;
import com.defi.common.util.log.entity.EventLog;
import com.defi.search.service.impl.SearchLogImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.vertx.core.Future;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class SearchManager {
    @Getter
    private static final SearchManager instance = new SearchManager();

    // Thay thế SearchQuerier bằng SearchLogImpl
    private final SearchLogImpl searchLog = SearchLogImpl.getInstance();

    private SearchManager() {
        // Khởi tạo SearchLogImpl nếu cần.
        // Giả sử nó đã được khởi tạo ở một nơi khác hoặc trong constructor của nó.
    }

    /**
     * Tìm kiếm EventLog theo khoảng thời gian.
     * Giả định các tham số đã được xác thực bởi lớp Handler.
     */
    public Future<BaseResponse<?>> findByDateRange(Long startTime, Long endTime) {
        List<EventLog> result = searchLog.findByDateRange(startTime, endTime);
        return Future.succeededFuture(BaseResponse.of(CommonError.SUCCESS, result));
    }

    /**
     * Tìm kiếm EventLog theo loại sự kiện (type) trong một khoảng thời gian.
     * Giả định các tham số đã được xác thực bởi lớp Handler.
     */
    public Future<BaseResponse<?>> findUserByType(String type, Long startTime, Long endTime) {
        List<EventLog> result = searchLog.findUserByType(type, startTime, endTime);
        return Future.succeededFuture(BaseResponse.of(CommonError.SUCCESS, result));
    }

    /**
     * Tìm kiếm EventLog theo subjectId.
     * Giả định subjectId đã được xác thực bởi lớp Handler.
     */
    public Future<BaseResponse<?>> findEventLogBySubjectId(String subjectId) {
        List<EventLog> result = searchLog.findEventLogBySubjectId(subjectId);
        return Future.succeededFuture(BaseResponse.of(CommonError.SUCCESS, result));
    }

    /**
     * Thống kê số lượng sự kiện trên mỗi target.
     * Giả định các tham số đã được xác thực bởi lớp Handler.
     */
    public Future<BaseResponse<?>> countEventsByTarget(Long startTime, Long endTime, int size) {
        List<?> result = searchLog.countEventsByTarget(startTime, endTime, size);
        return Future.succeededFuture(BaseResponse.of(CommonError.SUCCESS, result));
    }

    /**
     * Thống kê số lượng sự kiện trên mỗi type.
     * Giả định các tham số đã được xác thực bởi lớp Handler.
     */
    public Future<BaseResponse<?>> countEventsByType(Long startTime, Long endTime, int size) {
        List<?> result = searchLog.countEventsByType(startTime, endTime, size);
        return Future.succeededFuture(BaseResponse.of(CommonError.SUCCESS, result));
    }

    /**
     * Thống kê top người dùng thực hiện nhiều sự kiện nhất.
     * Giả định các tham số đã được xác thực bởi lớp Handler.
     */
    public Future<BaseResponse<?>> findTopUsersByEventCount(Long startTime, Long endTime, int size) {
        List<?> result = searchLog.findTopUsersByEventCount(startTime, endTime, size);
        return Future.succeededFuture(BaseResponse.of(CommonError.SUCCESS, result));
    }


    /**
     * Phương thức chung để điều phối các loại tìm kiếm và thống kê từ một request JSON.
     * Phương thức này gọi đến các phương thức khác trong cùng lớp,
     * vốn đã được đơn giản hóa để chỉ gọi đến SearchLogImpl.
     */
    public Future<BaseResponse<?>> searchWithParams(ObjectNode params) {
        // Validation cơ bản cho params và searchType nên được thực hiện ở lớp Handler.
        // Tuy nhiên, giữ lại ở đây để phòng trường hợp gọi trực tiếp.
        if (params == null) {
            return Future.succeededFuture(BaseResponse.of(CommonError.INVALID_PARAM, "Parameters cannot be null"));
        }

        JsonNode searchTypeNode = params.get("searchType");
        if (searchTypeNode == null || !searchTypeNode.isTextual()) {
            return Future.succeededFuture(BaseResponse.of(CommonError.INVALID_PARAM, "Missing or invalid 'searchType' parameter"));
        }

        String type = searchTypeNode.asText();
        Long fromTime = getLongParam(params, "fromTime");
        Long toTime = getLongParam(params, "toTime");
        int size = getIntParam(params, "size", 10); // Default size for stats

        // Switch-case đã được cập nhật để gọi các phương thức mới
        return switch (type) {
            case "byDateRange" -> findByDateRange(fromTime, toTime);
            case "byType" -> findUserByType(
                    getStringParam(params, "type"),
                    fromTime,
                    toTime
            );
            case "bySubjectId" -> findEventLogBySubjectId(
                    getStringParam(params, "subjectId")
            );
            case "countByTarget" -> countEventsByTarget(fromTime, toTime, size);
            case "countByType" -> countEventsByType(fromTime, toTime, size);
            case "topUsers" -> findTopUsersByEventCount(fromTime, toTime, size);
            default ->
                    Future.succeededFuture(BaseResponse.of(CommonError.INVALID_PARAM, "Unknown searchType: " + type));
        };
    }

    // Helper methods để parse parameters từ JSON
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