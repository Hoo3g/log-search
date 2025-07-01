package com.defi.config.vertx;

import com.defi.config.vertx.api.CatalogApi;
import com.defi.config.vertx.api.EformApi;
import io.vertx.ext.web.Router;

public class ConfigApi {
    public static void configAPI(Router router) {
        CatalogApi.configAPI(router);
        EformApi.configAPI(router);
    }
}
