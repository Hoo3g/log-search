package com.defi.config.vertx.api;

import com.defi.common.util.json.JsonUtil;
import com.defi.common.util.log.entity.EventLog;
import com.defi.common.vertx.HttpApi;
import com.defi.common.vertx.handler.ApiPermissionHandler;
import com.defi.common.vertx.handler.EventHandler;
import com.defi.config.catalog.CatalogManager;
import com.defi.config.catalog.dto.CatalogItemId;
import com.defi.config.catalog.entity.CatalogItem;
import com.defi.config.catalog.entity.CatalogType;
import com.defi.config.orchestrator.ConfigOrchestrator;
import com.defi.config.orchestrator.constant.ConfigPermission;
import com.defi.config.orchestrator.event.ConfigEntityType;
import com.defi.config.orchestrator.event.ConfigEventContext;
import com.defi.config.orchestrator.event.ConfigEventType;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

public class CatalogApi {

        public static void configAPI(Router router) {
                adminReadApi(router);
                adminWriteApi(router);
        }

        private static void adminReadApi(Router router) {
                // Get all catalog types
                router.get("/config/v1/admin/catalog/types")
                                .handler(ApiPermissionHandler.create(ConfigPermission.RESOURCE,
                                                ConfigPermission.ACTION_READ))
                                .handler(HttpApi.handleSync(ctx -> CatalogManager.getInstance().listCatalogTypes()));

                // Get catalog by type code (includes items)
                router.get("/config/v1/admin/catalog/:typeCode")
                                .handler(ApiPermissionHandler.create(ConfigPermission.RESOURCE,
                                                ConfigPermission.ACTION_READ))
                                .handler(HttpApi.handleSync(ctx -> {
                                        String typeCode = ctx.pathParam("typeCode");
                                        return CatalogManager.getInstance().getCatalogByTypeCode(typeCode);
                                }));
        }

        private static void adminWriteApi(Router router) {
                // Create catalog type
                router.post("/config/v1/admin/catalog/types")
                                .handler(BodyHandler.create(false))
                                .handler(ApiPermissionHandler.create(ConfigPermission.RESOURCE,
                                                ConfigPermission.ACTION_WRITE))
                                .handler(EventHandler.create(ConfigEntityType.CATALOG_TYPE,
                                                ConfigEventType.CATALOG_TYPE_CREATED))
                                .handler(HttpApi.handleSync(ctx -> {
                                        EventLog event = EventHandler.getEventFromRoutingContext(ctx);
                                        CatalogType catalogType = JsonUtil.fromJson(event.getData(), CatalogType.class);

                                        ConfigEventContext<CatalogType> eventContext = ConfigEventContext
                                                        .<CatalogType>builder()
                                                        .event(event)
                                                        .context(catalogType)
                                                        .build();

                                        return ConfigOrchestrator.getInstance().handleEvent(eventContext);
                                }));

                // Update catalog type
                router.put("/config/v1/admin/catalog/types/:code")
                                .handler(BodyHandler.create(false))
                                .handler(ApiPermissionHandler.create(ConfigPermission.RESOURCE,
                                                ConfigPermission.ACTION_WRITE))
                                .handler(EventHandler.create(ConfigEntityType.CATALOG_TYPE,
                                                ConfigEventType.CATALOG_TYPE_UPDATED))
                                .handler(HttpApi.handleSync(ctx -> {
                                        EventLog event = EventHandler.getEventFromRoutingContext(ctx);
                                        String code = ctx.pathParam("code");
                                        event.setTargetId(code);

                                        CatalogType catalogType = JsonUtil.fromJson(event.getData(), CatalogType.class);
                                        catalogType.setCode(code);

                                        ConfigEventContext<CatalogType> eventContext = ConfigEventContext
                                                        .<CatalogType>builder()
                                                        .event(event)
                                                        .context(catalogType)
                                                        .build();

                                        return ConfigOrchestrator.getInstance().handleEvent(eventContext);
                                }));

                // Delete catalog type
                router.delete("/config/v1/admin/catalog/types/:code")
                                .handler(ApiPermissionHandler.create(ConfigPermission.RESOURCE,
                                                ConfigPermission.ACTION_DELETE))
                                .handler(EventHandler.create(ConfigEntityType.CATALOG_TYPE,
                                                ConfigEventType.CATALOG_TYPE_DELETED))
                                .handler(HttpApi.handleSync(ctx -> {
                                        EventLog event = EventHandler.getEventFromRoutingContext(ctx);
                                        String code = ctx.pathParam("code");
                                        event.setTargetId(code);

                                        ConfigEventContext<String> eventContext = ConfigEventContext.<String>builder()
                                                        .event(event)
                                                        .context(code)
                                                        .build();

                                        return ConfigOrchestrator.getInstance().handleEvent(eventContext);
                                }));

                // Create catalog item
                router.post("/config/v1/admin/catalog/:typeCode/items")
                                .handler(BodyHandler.create(false))
                                .handler(ApiPermissionHandler.create(ConfigPermission.RESOURCE,
                                                ConfigPermission.ACTION_WRITE))
                                .handler(EventHandler.create(ConfigEntityType.CATALOG_ITEM,
                                                ConfigEventType.CATALOG_ITEM_CREATED))
                                .handler(HttpApi.handleSync(ctx -> {
                                        EventLog event = EventHandler.getEventFromRoutingContext(ctx);
                                        String typeCode = ctx.pathParam("typeCode");

                                        CatalogItem catalogItem = JsonUtil.fromJson(event.getData(), CatalogItem.class);
                                        catalogItem.setTypeCode(typeCode);

                                        ConfigEventContext<CatalogItem> eventContext = ConfigEventContext
                                                        .<CatalogItem>builder()
                                                        .event(event)
                                                        .context(catalogItem)
                                                        .build();

                                        return ConfigOrchestrator.getInstance().handleEvent(eventContext);
                                }));

                // Update catalog item
                router.put("/config/v1/admin/catalog/:typeCode/items/:itemCode")
                                .handler(BodyHandler.create(false))
                                .handler(ApiPermissionHandler.create(ConfigPermission.RESOURCE,
                                                ConfigPermission.ACTION_WRITE))
                                .handler(EventHandler.create(ConfigEntityType.CATALOG_ITEM,
                                                ConfigEventType.CATALOG_ITEM_UPDATED))
                                .handler(HttpApi.handleSync(ctx -> {
                                        EventLog event = EventHandler.getEventFromRoutingContext(ctx);
                                        String typeCode = ctx.pathParam("typeCode");
                                        String itemCode = ctx.pathParam("itemCode");

                                        CatalogItem catalogItem = JsonUtil.fromJson(event.getData(), CatalogItem.class);
                                        catalogItem.setTypeCode(typeCode);
                                        catalogItem.setCode(itemCode);

                                        ConfigEventContext<CatalogItem> eventContext = ConfigEventContext
                                                        .<CatalogItem>builder()
                                                        .event(event)
                                                        .context(catalogItem)
                                                        .build();

                                        return ConfigOrchestrator.getInstance().handleEvent(eventContext);
                                }));

                // Delete catalog item
                router.delete("/config/v1/admin/catalog/:typeCode/items/:itemCode")
                                .handler(ApiPermissionHandler.create(ConfigPermission.RESOURCE,
                                                ConfigPermission.ACTION_DELETE))
                                .handler(EventHandler.create(ConfigEntityType.CATALOG_ITEM,
                                                ConfigEventType.CATALOG_ITEM_DELETED))
                                .handler(HttpApi.handleSync(ctx -> {
                                        EventLog event = EventHandler.getEventFromRoutingContext(ctx);
                                        String typeCode = ctx.pathParam("typeCode");
                                        String itemCode = ctx.pathParam("itemCode");

                                        CatalogItemId itemToDelete = CatalogItemId.builder()
                                                        .typeCode(typeCode)
                                                        .code(itemCode)
                                                        .build();

                                        ConfigEventContext<CatalogItemId> eventContext = ConfigEventContext
                                                        .<CatalogItemId>builder()
                                                        .event(event)
                                                        .context(itemToDelete)
                                                        .build();

                                        return ConfigOrchestrator.getInstance().handleEvent(eventContext);
                                }));
        }
}
