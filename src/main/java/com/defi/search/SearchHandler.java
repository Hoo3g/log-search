package com.defi.search;

import com.defi.common.api.BaseResponse;
import com.defi.common.api.CommonError;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SearchHandler {

    private static final SearchManager searchManager = SearchManager.getInstance();
    private static final ObjectMapper objectMapper = new ObjectMapper();


    public static Future<BaseResponse<?>> handleFindByDateRange(RoutingContext ctx) {
        try {
            ObjectNode params = convertToObjectNode(ctx.body().asJsonObject());
            if (params == null) {
                return Future.succeededFuture(BaseResponse.of(CommonError.INVALID_PARAM, "Missing or invalid JSON body"));
            }

            Long fromTime = getLongParam(params, "fromTime");
            Long toTime = getLongParam(params, "toTime");

            if (fromTime == null || toTime == null || fromTime > toTime) {
                return Future.succeededFuture(BaseResponse.of(CommonError.INVALID_PARAM, "Invalid time range"));
            }

            return SearchManager.getInstance().findByDateRange(fromTime, toTime);

        } catch (Exception e) {
            log.error("Error in handleFindByDateRange", e);
            return Future.succeededFuture(BaseResponse.of(CommonError.INTERNAL_SERVER, "Unexpected error"));
        }
    }

    public static Future<BaseResponse<?>> handleFindUserByType(RoutingContext ctx) {
        try {
            ObjectNode params = convertToObjectNode(ctx.body().asJsonObject());
            if (params == null)
                return Future.succeededFuture(BaseResponse.of(CommonError.INVALID_PARAM,
                        "Missing or invalid JSON body"));

            String type = getStringParam(params, "type");
            Long fromTime = getLongParam(params, "fromTime");
            Long toTime = getLongParam(params, "toTime");

            if (isInvalid(type))
                return Future.succeededFuture(BaseResponse.of(CommonError.INVALID_PARAM,
                        "Missing or invalid 'type' parameter"));

            if (fromTime == null || toTime == null || fromTime > toTime) {
                return Future.succeededFuture(BaseResponse.of(CommonError.INVALID_PARAM, "Invalid time range"));
            }

            return searchManager.findUserByType(type, fromTime, toTime);
        } catch (Exception e) {
            log.error("Error in handleFindUserByType", e);
            return Future.succeededFuture(BaseResponse.of(CommonError.INTERNAL_SERVER, "Unexpected error"));
        }
    }

    public static Future<BaseResponse<?>> handleFindEventLogBySubjectId(RoutingContext ctx) {
        try {
            ObjectNode params = convertToObjectNode(ctx.body().asJsonObject());
            if (params == null)
                return Future.succeededFuture(BaseResponse.of(CommonError.INVALID_PARAM));

            String subjectId = getStringParam(params, "subjectId");

            if (isInvalid(subjectId))
                return Future.succeededFuture(BaseResponse.of(CommonError.INVALID_PARAM));

            return searchManager.findEventLogBySubjectId(subjectId);

        } catch (Exception e) {
            log.error("Error in handleFindEventLogBySubjectId", e);
            return Future.succeededFuture(BaseResponse.of(CommonError.INTERNAL_SERVER, "Unexpected error"));
        }
    }

    public static Future<BaseResponse<?>> handleCountEventsByTarget(RoutingContext ctx) {
        try {
            ObjectNode params = convertToObjectNode(ctx.body().asJsonObject());
            if (params == null)
                return Future.succeededFuture(BaseResponse.of(CommonError.INVALID_PARAM));

            Long fromTime = getLongParam(params, "fromTime");
            Long toTime = getLongParam(params, "toTime");
            int size = getIntParam(params, "size", 10);

            if (fromTime == null || toTime == null || fromTime > toTime)
                return Future.succeededFuture(BaseResponse.of(CommonError.INVALID_PARAM));

            if (size <= 0 || size > 1000)
                return Future.succeededFuture(BaseResponse.of(CommonError.INVALID_PARAM));

            return searchManager.countEventsByTarget(fromTime, toTime, size);

        } catch (Exception e) {
            log.error("Error in handleCountEventsByTarget", e);
            return Future.succeededFuture(BaseResponse.of(CommonError.INTERNAL_SERVER, "Unexpected error"));
        }
    }

    public static Future<BaseResponse<?>> handleCountEventsByType(RoutingContext ctx) {
        try {
            ObjectNode params = convertToObjectNode(ctx.body().asJsonObject());
            if (params == null)
                return Future.succeededFuture(BaseResponse.of(CommonError.INVALID_PARAM));

            Long fromTime = getLongParam(params, "fromTime");
            Long toTime = getLongParam(params, "toTime");
            int size = getIntParam(params, "size", 10);

            if (fromTime == null || toTime == null || fromTime > toTime)
                return Future.succeededFuture(BaseResponse.of(CommonError.INVALID_PARAM));

            if (size <= 0 || size > 1000)
                return Future.succeededFuture(BaseResponse.of(CommonError.INVALID_PARAM));

            return searchManager.countEventsByType(fromTime, toTime, size);

        } catch (Exception e) {
            log.error("Error in handleCountEventsByTarget", e);
            return Future.succeededFuture(BaseResponse.of(CommonError.INTERNAL_SERVER, "Unexpected error"));
        }
    }

    public static Future<BaseResponse<?>> handleFindTopUsersByEventCount(RoutingContext ctx) {
        try {
            ObjectNode params = convertToObjectNode(ctx.body().asJsonObject());
            if (params == null)
                return Future.succeededFuture(BaseResponse.of(CommonError.INVALID_PARAM));

            Long fromTime = getLongParam(params, "fromTime");
            Long toTime = getLongParam(params, "toTime");
            int size = getIntParam(params, "size", 10);

            if (fromTime == null || toTime == null || fromTime > toTime)
                return Future.succeededFuture(BaseResponse.of(CommonError.INVALID_PARAM));

            if (size <= 0 || size > 1000)
                return Future.succeededFuture(BaseResponse.of(CommonError.INVALID_PARAM));

            return searchManager.findTopUsersByEventCount(fromTime, toTime, size);

        } catch (Exception e) {
            log.error("Error in handleCountEventsByTarget", e);
            return Future.succeededFuture(BaseResponse.of(CommonError.INTERNAL_SERVER, "Unexpected error"));
        }
    }

    // Helper methods
    private static ObjectNode convertToObjectNode(JsonObject vertxJson) {
        try {
            // Convert Vert.x JsonObject to Jackson ObjectNode
            String jsonString = vertxJson.encode();
            return (ObjectNode) objectMapper.readTree(jsonString);
        } catch (Exception e) {
            log.error("Error converting JsonObject to ObjectNode", e);
            return null;
        }
    }

    private static boolean isInvalid(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static String getStringParam(ObjectNode params, String key) {
        JsonNode node = params.get(key);
        return node != null && node.isTextual() ? node.asText() : null;
    }

    private static Long getLongParam(ObjectNode params, String key) {
        JsonNode node = params.get(key);
        return node != null && node.isNumber() ? node.asLong() : null;
    }

    private static int getIntParam(ObjectNode params, String key, int defaultValue) {
        JsonNode node = params.get(key);
        return node != null && node.isNumber() ? node.asInt() : defaultValue;
    }

}