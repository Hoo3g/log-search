package com.defi.config.orchestrator;

import com.defi.common.api.BaseResponse;
import com.defi.config.catalog.CatalogManager;
import com.defi.config.catalog.dto.CatalogItemId;
import com.defi.config.catalog.entity.CatalogItem;
import com.defi.config.catalog.entity.CatalogType;
import com.defi.config.eform.EformManager;
import com.defi.config.eform.entity.Eform;
import com.defi.config.orchestrator.event.ConfigEventContext;
import com.defi.config.orchestrator.event.ConfigEventType;
import com.defi.common.util.log.entity.EventLog;
import lombok.Getter;

public class ConfigOrchestrator {
        @Getter
        private static final ConfigOrchestrator instance = new ConfigOrchestrator();

        private ConfigOrchestrator() {
                // private constructor for singleton
        }

        @SuppressWarnings("unchecked")
        public BaseResponse<?> handleEvent(ConfigEventContext<?> eventContext) {
                EventLog event = eventContext.getEvent();
                return switch (event.getType()) {
                        case ConfigEventType.CATALOG_TYPE_CREATED -> CatalogManager.getInstance()
                                        .catalogTypeCreated((ConfigEventContext<CatalogType>) eventContext);
                        case ConfigEventType.CATALOG_TYPE_UPDATED -> CatalogManager.getInstance()
                                        .catalogTypeUpdated((ConfigEventContext<CatalogType>) eventContext);
                        case ConfigEventType.CATALOG_TYPE_DELETED -> CatalogManager.getInstance()
                                        .catalogTypeDeleted((ConfigEventContext<String>) eventContext);
                        case ConfigEventType.CATALOG_ITEM_CREATED -> CatalogManager.getInstance()
                                        .catalogItemCreated((ConfigEventContext<CatalogItem>) eventContext);
                        case ConfigEventType.CATALOG_ITEM_UPDATED -> CatalogManager.getInstance()
                                        .catalogItemUpdated((ConfigEventContext<CatalogItem>) eventContext);
                        case ConfigEventType.CATALOG_ITEM_DELETED -> CatalogManager.getInstance()
                                        .catalogItemDeleted((ConfigEventContext<CatalogItemId>) eventContext);
                        case ConfigEventType.EFORM_CREATED -> EformManager.getInstance()
                                        .eformCreated((ConfigEventContext<Eform>) eventContext);
                        case ConfigEventType.EFORM_UPDATED -> EformManager.getInstance()
                                        .eformUpdated((ConfigEventContext<Eform>) eventContext);
                        case ConfigEventType.EFORM_DELETED -> EformManager.getInstance()
                                        .eformDeleted((ConfigEventContext<String>) eventContext);
                        default -> BaseResponse.INTERNAL_SERVER_ERROR;
                };
        }
}
