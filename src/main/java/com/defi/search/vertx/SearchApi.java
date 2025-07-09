package com.defi.search.vertx;

import com.defi.common.api.BaseResponse;
import com.defi.common.api.CommonError;
import com.defi.common.util.json.JsonUtil;
import com.defi.common.vertx.HttpApi;
import com.defi.common.vertx.handler.ApiPermissionHandler;
import com.defi.search.SearchManager;
import com.defi.config.orchestrator.constant.ConfigPermission;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SearchApi {

    public static void configAPI(Router router) {
        adminSearchApi(router);
        publicSearchApi(router);
    }

    private static void adminSearchApi(Router router) {
        // Unified search endpoint với parameters
        router.post("/search/v1/admin/query")
                .handler(BodyHandler.create(false))
                .handler(ApiPermissionHandler.create(ConfigPermission.RESOURCE, ConfigPermission.ACTION_READ))
                .handler(HttpApi.handleAsync(ctx -> {
                    ObjectNode params = JsonUtil.toJsonObject(ctx.body().asString());
                    return SearchManager.getInstance().searchWithParams(params);
                }));

        // Event type statistics
        router.get("/search/v1/admin/stats/event-types")
                .handler(ApiPermissionHandler.create(ConfigPermission.RESOURCE, ConfigPermission.ACTION_READ))
                .handler(HttpApi.handleAsync(ctx -> {
                    return SearchManager.getInstance().getEventTypeStats();
                }));
    }

    private static void publicSearchApi(Router router) {
        // Tìm kiếm theo ID
        router.get("/search/v1/public/event/:id")
                .handler(ApiPermissionHandler.create(ConfigPermission.RESOURCE, ConfigPermission.ACTION_READ))
                .handler(HttpApi.handleAsync(ctx -> {
                    String id = ctx.pathParam("id");
                    return SearchManager.getInstance().searchById(id);
                }));

        // Tìm kiếm logs gần đây
        router.get("/search/v1/public/recent")
                //.handler(ApiPermissionHandler.create(ConfigPermission.RESOURCE, ConfigPermission.ACTION_READ))
                .handler(HttpApi.handleAsync(ctx -> {
                    String sizeParam = ctx.request().getParam("size");
                    int size = sizeParam != null ? Integer.parseInt(sizeParam) : 10;
                    return SearchManager.getInstance().searchRecentLogs(size);
                }));

        // Tìm kiếm theo target
        router.get("/search/v1/public/target")
                .handler(ApiPermissionHandler.create(ConfigPermission.RESOURCE, ConfigPermission.ACTION_READ))
                .handler(HttpApi.handleAsync(ctx -> {
                    String targetType = ctx.request().getParam("targetType");
                    String targetId = ctx.request().getParam("targetId");
                    return SearchManager.getInstance().searchByTarget(targetType, targetId);
                }));

        // Tìm kiếm theo subject
        router.get("/search/v1/public/subject")
                //.handler(ApiPermissionHandler.create(ConfigPermission.RESOURCE, ConfigPermission.ACTION_READ))
                .handler(HttpApi.handleAsync(ctx -> {
                    String subjectType = ctx.request().getParam("subjectType");
                    String subjectId = ctx.request().getParam("subjectId");
                    return SearchManager.getInstance().searchBySubject(subjectType, subjectId);
                }));

        // Tìm kiếm theo event type
        router.get("/search/v1/public/event-type/:type")
                .handler(ApiPermissionHandler.create(ConfigPermission.RESOURCE, ConfigPermission.ACTION_READ))
                .handler(HttpApi.handleAsync(ctx -> {
                    String eventType = ctx.pathParam("type");
                    return SearchManager.getInstance().searchByEventType(eventType);
                }));

        // Tìm kiếm theo correlation ID
        router.get("/search/v1/public/correlation/:correlationId")
                .handler(ApiPermissionHandler.create(ConfigPermission.RESOURCE, ConfigPermission.ACTION_READ))
                .handler(HttpApi.handleAsync(ctx -> {
                    String correlationId = ctx.pathParam("correlationId");
                    return SearchManager.getInstance().searchByCorrelationId(correlationId);
                }));

        // Tìm kiếm theo khoảng thời gian
        router.get("/search/v1/public/time-range")
                .handler(ApiPermissionHandler.create(ConfigPermission.RESOURCE, ConfigPermission.ACTION_READ))
                .handler(HttpApi.handleAsync(ctx -> {
                    String fromTimeStr = ctx.request().getParam("fromTime");
                    String toTimeStr = ctx.request().getParam("toTime");

                    try {
                        Long fromTime = fromTimeStr != null ? Long.parseLong(fromTimeStr) : null;
                        Long toTime = toTimeStr != null ? Long.parseLong(toTimeStr) : null;
                        return SearchManager.getInstance().searchByTimeRange(fromTime, toTime);
                    } catch (NumberFormatException e) {
                        return Future.succeededFuture(BaseResponse.of(CommonError.INVALID_PARAM,
                                "Invalid time format. Use timestamp in milliseconds"
                        ));
                    }
                }));

        // Full-text search
        router.get("/search/v1/public/full-text")
                //.handler(ApiPermissionHandler.create(ConfigPermission.RESOURCE, ConfigPermission.ACTION_READ))
                .handler(HttpApi.handleAsync(ctx -> {
                    String searchText = ctx.request().getParam("q");
                    return SearchManager.getInstance().searchFullText(searchText);
                }));

        // Tìm kiếm trong data field
        router.get("/search/v1/public/data")
                .handler(ApiPermissionHandler.create(ConfigPermission.RESOURCE, ConfigPermission.ACTION_READ))
                .handler(HttpApi.handleAsync(ctx -> {
                    String dataField = ctx.request().getParam("dataField");
                    String value = ctx.request().getParam("value");
                    return SearchManager.getInstance().searchInData(dataField, value);
                }));

        // Tìm kiếm kết hợp nhiều điều kiện
        router.get("/search/v1/public/multi-conditions")
                .handler(ApiPermissionHandler.create(ConfigPermission.RESOURCE, ConfigPermission.ACTION_READ))
                .handler(HttpApi.handleAsync(ctx -> {
                    String targetType = ctx.request().getParam("targetType");
                    String eventType = ctx.request().getParam("eventType");
                    String fromTimeStr = ctx.request().getParam("fromTime");
                    String toTimeStr = ctx.request().getParam("toTime");

                    try {
                        Long fromTime = fromTimeStr != null ? Long.parseLong(fromTimeStr) : null;
                        Long toTime = toTimeStr != null ? Long.parseLong(toTimeStr) : null;
                        return SearchManager.getInstance().searchMultipleConditions(targetType, eventType, fromTime, toTime);
                    } catch (NumberFormatException e) {
                        return Future.succeededFuture(BaseResponse.of(CommonError.INVALID_PARAM,
                                "Invalid time format. Use timestamp in milliseconds"
                        ));
                    }
                }));
    }
}