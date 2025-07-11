package com.defi.search.vertx;

import com.defi.common.vertx.HttpApi;
import com.defi.search.SearchHandler;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SearchApi {

    public static void configAPI(Router router) {
        searchApi(router);
    }

    private static void searchApi(Router router) {
        router.post("/admin/api/search/by-date-range")
                .handler(BodyHandler.create())
                .handler(HttpApi.handleAsync(SearchHandler::handleFindByDateRange));

        router.post("/admin/api/search/user-by-type")
                .handler(BodyHandler.create())
                .handler(HttpApi.handleAsync(SearchHandler::handleFindUserByType));

        router.post("/admin/api/search/log-by-subject-id")
                .handler(BodyHandler.create())
                .handler(HttpApi.handleAsync(SearchHandler::handleFindEventLogBySubjectId));

        router.post("/admin/api/search/count-by-target")
                .handler(BodyHandler.create())
                .handler(HttpApi.handleAsync(SearchHandler::handleCountEventsByTarget));

        router.post("/admin/api/search/count-by-type")
                .handler(BodyHandler.create())
                .handler(HttpApi.handleAsync(SearchHandler::handleCountEventsByType));

        router.post("/admin/api/search/top-users")
                .handler(BodyHandler.create())
                .handler(HttpApi.handleAsync(SearchHandler::handleFindTopUsersByEventCount));

        log.info("Search API endpoints configured successfully");
    }
}