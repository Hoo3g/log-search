package com.defi.search.vertx;

import com.defi.common.util.json.JsonUtil;
import com.defi.common.vertx.handler.ApiPermissionHandler;
import com.defi.search.SearchManager;
import com.defi.config.orchestrator.constant.ConfigPermission;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

public class SearchApi {

    public static void configAPI(Router router) {
        adminSearchApi(router);
        publicSearchApi(router);
    }

    private static void adminSearchApi(Router router) {

        // Unified search endpoint với parameters
        router.post("/search/v1/admin/query")
                //.handler(BodyHandler.create(false))
                //.handler(ApiPermissionHandler.create(ConfigPermission.RESOURCE, ConfigPermission.ACTION_READ))
                .handler(ctx -> {
                    ObjectNode params = JsonUtil.toJsonObject(ctx.body().asString());
                    if (params == null) {
                        ctx.response().setStatusCode(400).end("Invalid JSON in request body");
                        return;
                    }

                    SearchManager.getInstance().searchWithParams(params)
                            .onSuccess(result -> {
                                ctx.response()
                                        .putHeader("content-type", "application/json")
                                        .end(result.toString());
                            })
                            .onFailure(error -> {
                                ctx.response().setStatusCode(500).end("Search failed: " + error.getMessage());
                            });
                });

        // Event type statistics
        router.get("/search/v1/admin/stats/event-types")
                //.handler(ApiPermissionHandler.create(ConfigPermission.RESOURCE, ConfigPermission.ACTION_READ))
                .handler(ctx -> {
                    SearchManager.getInstance().getEventTypeStats()
                            .onSuccess(result -> {
                                ctx.response()
                                        .putHeader("content-type", "application/json")
                                        .end(result.toString());
                            })
                            .onFailure(error -> {
                                ctx.response().setStatusCode(500).end("Search failed: " + error.getMessage());
                            });
                });
    }

    private static void publicSearchApi(Router router) {
        // Tìm kiếm theo ID - có thể public nếu cần
        router.get("/search/v1/public/event/:id")
                //.handler(ApiPermissionHandler.create(ConfigPermission.RESOURCE, ConfigPermission.ACTION_READ))
                .handler(ctx -> {
                    String id = ctx.pathParam("id");
                    SearchManager.getInstance().searchById(id)
                            .onSuccess(result -> {
                                ctx.response()
                                        .putHeader("content-type", "application/json")
                                        .end(result.toString());
                            })
                            .onFailure(error -> {
                                ctx.response().setStatusCode(500).end("Search failed: " + error.getMessage());
                            });
                });

        // Tìm kiếm logs gần đây
        router.get("/search/v1/public/recent")
                //.handler(ApiPermissionHandler.create(ConfigPermission.RESOURCE, ConfigPermission.ACTION_READ))
                .handler(ctx -> {
                    String sizeParam = ctx.request().getParam("size");
                    int size = sizeParam != null ? Integer.parseInt(sizeParam) : 10;

                    SearchManager.getInstance().searchRecentLogs(size)
                            .onSuccess(result -> {
                                ctx.response()
                                        .putHeader("content-type", "application/json")
                                        .end(result.toString());
                            })
                            .onFailure(error -> {
                                ctx.response().setStatusCode(500).end("Search failed: " + error.getMessage());
                            });
                });

        router.get("/search/v1/public/target")
//                .handler(ApiPermissionHandler.create(ConfigPermission.RESOURCE, ConfigPermission.ACTION_READ))
                .handler(ctx -> {
                    String targetType = ctx.request().getParam("targetType");
                    String targetId = ctx.request().getParam("targetId");

                    if (targetType == null || targetId == null) {
                        ctx.response().setStatusCode(400).end("Missing targetType or targetId parameter");
                        return;
                    }

                    SearchManager.getInstance().searchByTarget(targetType, targetId)
                            .onSuccess(result -> {
                                ctx.response()
                                        .putHeader("content-type", "application/json")
                                        .end(result.toString());
                            })
                            .onFailure(error -> {
                                ctx.response().setStatusCode(500).end("Search failed: " + error.getMessage());
                            });
                });

        // Tìm kiếm theo subject (GET with query params)
        router.get("/search/v1/public/subject")
                //.handler(ApiPermissionHandler.create(ConfigPermission.RESOURCE, ConfigPermission.ACTION_READ))
                .handler(ctx -> {
                    String subjectType = ctx.request().getParam("subjectType");
                    String subjectId = ctx.request().getParam("subjectId");

                    if (subjectType == null || subjectId == null) {
                        ctx.response().setStatusCode(400).end("Missing subjectType or subjectId parameter");
                        return;
                    }

                    SearchManager.getInstance().searchBySubject(subjectType, subjectId)
                            .onSuccess(result -> {
                                ctx.response()
                                        .putHeader("content-type", "application/json")
                                        .end(result.toString());
                            })
                            .onFailure(error -> {
                                ctx.response().setStatusCode(500).end("Search failed: " + error.getMessage());
                            });
                });

        // Tìm kiếm theo event type
        router.get("/search/v1/public/event-type/:type")
                //.handler(ApiPermissionHandler.create(ConfigPermission.RESOURCE, ConfigPermission.ACTION_READ))
                .handler(ctx -> {
                    String eventType = ctx.pathParam("type");
                    SearchManager.getInstance().searchByEventType(eventType)
                            .onSuccess(result -> {
                                ctx.response()
                                        .putHeader("content-type", "application/json")
                                        .end(result.toString());
                            })
                            .onFailure(error -> {
                                ctx.response().setStatusCode(500).end("Search failed: " + error.getMessage());
                            });
                });

        // Tìm kiếm theo correlation ID
        router.get("/search/v1/public/correlation/:correlationId")
                //.handler(ApiPermissionHandler.create(ConfigPermission.RESOURCE, ConfigPermission.ACTION_READ))
                .handler(ctx -> {
                    String correlationId = ctx.pathParam("correlationId");
                    SearchManager.getInstance().searchByCorrelationId(correlationId)
                            .onSuccess(result -> {
                                ctx.response()
                                        .putHeader("content-type", "application/json")
                                        .end(result.toString());
                            })
                            .onFailure(error -> {
                                ctx.response().setStatusCode(500).end("Search failed: " + error.getMessage());
                            });
                });

        // Tìm kiếm theo khoảng thời gian
        router.get("/search/v1/public/time-range")
                //.handler(ApiPermissionHandler.create(ConfigPermission.RESOURCE, ConfigPermission.ACTION_READ))
                .handler(ctx -> {
                    String fromTimeStr = ctx.request().getParam("fromTime");
                    String toTimeStr = ctx.request().getParam("toTime");

                    if (fromTimeStr == null || toTimeStr == null) {
                        ctx.response().setStatusCode(400).end("Missing fromTime or toTime parameter");
                        return;
                    }

                    try {
                        Long fromTime = Long.parseLong(fromTimeStr);
                        Long toTime = Long.parseLong(toTimeStr);

                        SearchManager.getInstance().searchByTimeRange(fromTime, toTime)
                                .onSuccess(result -> {
                                    ctx.response()
                                            .putHeader("content-type", "application/json")
                                            .end(result.toString());
                                })
                                .onFailure(error -> {
                                    ctx.response().setStatusCode(500).end("Search failed: " + error.getMessage());
                                });
                    } catch (NumberFormatException e) {
                        ctx.response().setStatusCode(400).end("Invalid time format. Use timestamp in milliseconds");
                    }
                });

        // Full-text search
        router.get("/search/v1/public/full-text")
                //.handler(ApiPermissionHandler.create(ConfigPermission.RESOURCE, ConfigPermission.ACTION_READ))
                .handler(ctx -> {
                    String searchText = ctx.request().getParam("q");
                    if (searchText == null || searchText.trim().isEmpty()) {
                        ctx.response().setStatusCode(400).end("Missing search query parameter 'q'");
                        return;
                    }

                    SearchManager.getInstance().searchFullText(searchText)
                            .onSuccess(result -> {
                                ctx.response()
                                        .putHeader("content-type", "application/json")
                                        .end(result.toString());
                            })
                            .onFailure(error -> {
                                ctx.response().setStatusCode(500).end("Search failed: " + error.getMessage());
                            });
                });
    }
}