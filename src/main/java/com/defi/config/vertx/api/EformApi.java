package com.defi.config.vertx.api;

import com.defi.common.util.json.JsonUtil;
import com.defi.common.util.log.entity.EventLog;
import com.defi.common.vertx.HttpApi;
import com.defi.common.vertx.handler.ApiPermissionHandler;
import com.defi.common.vertx.handler.EventHandler;
import com.defi.config.eform.dto.EformFilter;
import com.defi.config.eform.EformManager;
import com.defi.config.eform.entity.Eform;
import com.defi.config.orchestrator.ConfigOrchestrator;
import com.defi.config.orchestrator.constant.ConfigPermission;
import com.defi.config.orchestrator.event.ConfigEntityType;
import com.defi.config.orchestrator.event.ConfigEventContext;
import com.defi.config.orchestrator.event.ConfigEventType;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

public class EformApi {

        public static void configAPI(Router router) {
                adminReadApi(router);
                adminWriteApi(router);
        }

        private static void adminReadApi(Router router) {
                // Get all eforms
                router.get("/config/v1/admin/eforms")
                                .handler(ApiPermissionHandler.create(ConfigPermission.RESOURCE,
                                                ConfigPermission.ACTION_READ))
                                .handler(HttpApi.handleSync(ctx -> EformManager.getInstance().listAllEforms()));

                // Get eform by code
                router.get("/config/v1/admin/eforms/:code")
                                .handler(ApiPermissionHandler.create(ConfigPermission.RESOURCE,
                                                ConfigPermission.ACTION_READ))
                                .handler(HttpApi.handleSync(ctx -> {
                                        String code = ctx.pathParam("code");
                                        return EformManager.getInstance().getEformByCode(code);
                                }));

                // Filter eforms with pagination
                router.post("/config/v1/admin/eforms/filter")
                                .handler(BodyHandler.create(false))
                                .handler(ApiPermissionHandler.create(ConfigPermission.RESOURCE,
                                                ConfigPermission.ACTION_READ))
                                .handler(HttpApi.handleSync(ctx -> {
                                        EformFilter filter = JsonUtil.fromJson(ctx.body().asString(),
                                                        EformFilter.class);
                                        return EformManager.getInstance().filterEforms(filter);
                                }));
        }

        private static void adminWriteApi(Router router) {
                // Create eform
                router.post("/config/v1/admin/eforms")
                                .handler(BodyHandler.create(false))
                                .handler(ApiPermissionHandler.create(ConfigPermission.RESOURCE,
                                                ConfigPermission.ACTION_WRITE))
                                .handler(EventHandler.create(ConfigEntityType.EFORM,
                                                ConfigEventType.EFORM_CREATED))
                                .handler(HttpApi.handleSync(ctx -> {
                                        EventLog event = EventHandler.getEventFromRoutingContext(ctx);
                                        Eform eform = JsonUtil.fromJson(event.getData(), Eform.class);

                                        ConfigEventContext<Eform> eventContext = ConfigEventContext
                                                        .<Eform>builder()
                                                        .event(event)
                                                        .context(eform)
                                                        .build();

                                        return ConfigOrchestrator.getInstance().handleEvent(eventContext);
                                }));

                // Update eform
                router.put("/config/v1/admin/eforms/:code")
                                .handler(BodyHandler.create(false))
                                .handler(ApiPermissionHandler.create(ConfigPermission.RESOURCE,
                                                ConfigPermission.ACTION_WRITE))
                                .handler(EventHandler.create(ConfigEntityType.EFORM,
                                                ConfigEventType.EFORM_UPDATED))
                                .handler(HttpApi.handleSync(ctx -> {
                                        EventLog event = EventHandler.getEventFromRoutingContext(ctx);
                                        String code = ctx.pathParam("code");
                                        event.setTargetId(code);

                                        Eform eform = JsonUtil.fromJson(event.getData(), Eform.class);
                                        eform.setCode(code);

                                        ConfigEventContext<Eform> eventContext = ConfigEventContext
                                                        .<Eform>builder()
                                                        .event(event)
                                                        .context(eform)
                                                        .build();

                                        return ConfigOrchestrator.getInstance().handleEvent(eventContext);
                                }));

                // Delete eform
                router.delete("/config/v1/admin/eforms/:code")
                                .handler(ApiPermissionHandler.create(ConfigPermission.RESOURCE,
                                                ConfigPermission.ACTION_DELETE))
                                .handler(EventHandler.create(ConfigEntityType.EFORM,
                                                ConfigEventType.EFORM_DELETED))
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
        }
}