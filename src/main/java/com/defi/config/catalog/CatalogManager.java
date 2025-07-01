package com.defi.config.catalog;

import com.defi.common.api.BaseResponse;
import com.defi.common.api.CommonError;
import com.defi.common.util.jdbi.JdbiProvider;
import com.defi.common.util.json.JsonUtil;
import com.defi.common.util.log.EventLogger;
import com.defi.common.util.log.entity.EventLog;
import com.defi.config.ConfigSharedServices;
import com.defi.config.catalog.dto.Catalog;
import com.defi.config.catalog.dto.CatalogItemId;
import com.defi.config.catalog.entity.CatalogItem;
import com.defi.config.catalog.entity.CatalogType;
import com.defi.config.orchestrator.event.ConfigEventContext;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import java.util.List;

@Slf4j
public class CatalogManager {
    @Getter
    private static final CatalogManager instance = new CatalogManager();

    private CatalogManager() {
    }

    public BaseResponse<?> catalogTypeCreated(ConfigEventContext<CatalogType> eventContext) {
        EventLog event = eventContext.getEvent();
        CatalogType catalogType = eventContext.getContext();
        return JdbiProvider.getInstance().getJdbi().inTransaction(handle -> {
            CatalogType created = ConfigSharedServices.catalogService.createType(handle, catalogType);
            event.setTargetId(created.getCode());
            EventLogger.log(event);
            return BaseResponse.of(CommonError.SUCCESS, created);
        });
    }

    public BaseResponse<?> catalogTypeUpdated(ConfigEventContext<CatalogType> eventContext) {
        EventLog event = eventContext.getEvent();
        CatalogType catalogType = eventContext.getContext();
        return JdbiProvider.getInstance().getJdbi().inTransaction(handle -> {
            boolean updated = ConfigSharedServices.catalogService.updateType(handle, catalogType);
            if (updated) {
                event.setTargetId(catalogType.getCode());
                EventLogger.log(event);
                return BaseResponse.of(CommonError.SUCCESS, catalogType);
            } else {
                return BaseResponse.of(CommonError.BAD_REQUEST);
            }
        });
    }

    public BaseResponse<?> catalogTypeDeleted(ConfigEventContext<String> eventContext) {
        EventLog event = eventContext.getEvent();
        String typeCode = eventContext.getContext();
        return JdbiProvider.getInstance().getJdbi().inTransaction(handle -> {
            boolean deleted = ConfigSharedServices.catalogService.deleteType(handle, typeCode);
            if (deleted) {
                event.setTargetId(typeCode);
                EventLogger.log(event);
                return BaseResponse.of(CommonError.SUCCESS);
            } else {
                return BaseResponse.of(CommonError.BAD_REQUEST);
            }
        });
    }

    public BaseResponse<?> catalogItemCreated(ConfigEventContext<CatalogItem> eventContext) {
        EventLog event = eventContext.getEvent();
        CatalogItem catalogItem = eventContext.getContext();
        return JdbiProvider.getInstance().getJdbi().inTransaction(handle -> {
            CatalogItem created = ConfigSharedServices.catalogService.createItem(handle, catalogItem);
            CatalogItemId itemId = CatalogItemId.builder().typeCode(created.getTypeCode()).code(created.getCode())
                    .build();
            event.setTargetId(JsonUtil.toJsonString(itemId));
            EventLogger.log(event);
            return BaseResponse.of(CommonError.SUCCESS, created);
        });
    }

    public BaseResponse<?> catalogItemUpdated(ConfigEventContext<CatalogItem> eventContext) {
        EventLog event = eventContext.getEvent();
        CatalogItem catalogItem = eventContext.getContext();
        return JdbiProvider.getInstance().getJdbi().inTransaction(handle -> {
            boolean updated = ConfigSharedServices.catalogService.updateItem(handle, catalogItem);
            if (updated) {
                CatalogItemId itemId = CatalogItemId.builder().typeCode(catalogItem.getTypeCode())
                        .code(catalogItem.getCode()).build();
                event.setTargetId(JsonUtil.toJsonString(itemId));
                EventLogger.log(event);
                return BaseResponse.of(CommonError.SUCCESS, catalogItem);
            } else {
                return BaseResponse.of(CommonError.BAD_REQUEST);
            }
        });
    }

    public BaseResponse<?> catalogItemDeleted(ConfigEventContext<CatalogItemId> eventContext) {
        EventLog event = eventContext.getEvent();
        CatalogItemId itemToDelete = eventContext.getContext();
        return JdbiProvider.getInstance().getJdbi().inTransaction(handle -> {
            boolean deleted = ConfigSharedServices.catalogService.deleteItem(handle, itemToDelete.getTypeCode(),
                    itemToDelete.getCode());
            if (deleted) {
                event.setTargetId(JsonUtil.toJsonString(itemToDelete));
                EventLogger.log(event);
                return BaseResponse.of(CommonError.SUCCESS);
            } else {
                return BaseResponse.of(CommonError.BAD_REQUEST);
            }
        });
    }

    public BaseResponse<?> getCatalogByTypeCode(String typeCode) {
        Catalog catalog = ConfigSharedServices.catalogService.getCatalogByTypeCode(typeCode);
        if (catalog == null) {
            return BaseResponse.of(CommonError.BAD_REQUEST);
        }
        return BaseResponse.of(CommonError.SUCCESS, catalog);
    }

    public BaseResponse<?> listCatalogTypes() {
        List<CatalogType> types = ConfigSharedServices.catalogService.listAllTypes();
        return BaseResponse.of(CommonError.SUCCESS, types);
    }
}
