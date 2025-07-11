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

    }


    public Future<BaseResponse<?>> findByDateRange(Long startTime, Long endTime) {
        List<EventLog> result = searchLog.findByDateRange(startTime, endTime);
        return Future.succeededFuture(BaseResponse.of(CommonError.SUCCESS, result));
    }

    /**
     * Tìm kiếm EventLog theo loại sự kiện (type) trong một khoảng thời gian.
     */
    public Future<BaseResponse<?>> findUserByType(String type, Long startTime, Long endTime) {
        List<EventLog> result = searchLog.findUserByType(type, startTime, endTime);
        return Future.succeededFuture(BaseResponse.of(CommonError.SUCCESS, result));
    }

    /**
     * Tìm kiếm EventLog theo subjectId.
     */
    public Future<BaseResponse<?>> findEventLogBySubjectId(String subjectId) {
        List<EventLog> result = searchLog.findEventLogBySubjectId(subjectId);
        return Future.succeededFuture(BaseResponse.of(CommonError.SUCCESS, result));
    }

    /**
     * Thống kê số lượng sự kiện trên mỗi target.
     */
    public Future<BaseResponse<?>> countEventsByTarget(Long startTime, Long endTime, int size) {
        List<?> result = searchLog.countEventsByTarget(startTime, endTime, size);
        return Future.succeededFuture(BaseResponse.of(CommonError.SUCCESS, result));
    }

    /**
     * Thống kê số lượng sự kiện trên mỗi type.
     */
    public Future<BaseResponse<?>> countEventsByType(Long startTime, Long endTime, int size) {
        List<?> result = searchLog.countEventsByType(startTime, endTime, size);
        return Future.succeededFuture(BaseResponse.of(CommonError.SUCCESS, result));
    }

    /**
     * Thống kê top người dùng thực hiện nhiều sự kiện nhất.
     */
    public Future<BaseResponse<?>> findTopUsersByEventCount(Long startTime, Long endTime, int size) {
        List<?> result = searchLog.findTopUsersByEventCount(startTime, endTime, size);
        return Future.succeededFuture(BaseResponse.of(CommonError.SUCCESS, result));
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