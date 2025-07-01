package com.defi.search.vertx;

import com.defi.common.token.entity.Token;
import com.defi.common.vertx.VertxConfig;
import com.defi.common.vertx.handler.TokenAuthHandler;
import com.defi.config.vertx.ConfigApi;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.CorsHandler;

import java.util.Set;

public class SearchVerticle  extends AbstractVerticle {
    private HttpServer httpServer;

    @Override
    public void start() {
        Router router = Router.router(vertx);
        crossAccessControl(router);
        router.route().handler(this::secureHandler);
        router.get("/search/v1/public/version").handler(this::version);

        //ConfigApi.configAPI(router);
        SearchApi.configAPI(router);
        httpServer = vertx.createHttpServer()
                .requestHandler(router)
                .listen(VertxConfig.instance().httpPort).result();


    }

    @Override
    public void stop() {
        if (httpServer != null)
            httpServer.close();
    }

    public void crossAccessControl(Router router)  {
        router.route().handler(CorsHandler.create()
                .addOrigin("*")
                .allowedHeaders(Set.of("*"))
                .allowedMethods(
                        Set.of(HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.PATCH, HttpMethod.DELETE, HttpMethod.OPTIONS))
                .allowCredentials(true));
    }

    private void version(RoutingContext rc) {
        rc.response().end("v1.0.0");
    }

    private void secureHandler(RoutingContext rc) {
        String path = rc.normalizedPath();
        if (!path.startsWith("/search/v1/public/")) {
            TokenAuthHandler.handle(rc);
        } else {
            rc.next();
        }
    }
}