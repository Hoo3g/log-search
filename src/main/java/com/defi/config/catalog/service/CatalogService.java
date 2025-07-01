package com.defi.config.catalog.service;

import com.defi.config.catalog.entity.CatalogType;
import com.defi.config.catalog.entity.CatalogItem;
import com.defi.config.catalog.dto.Catalog;
import org.jdbi.v3.core.Handle;

import java.util.List;

public interface CatalogService {

    // CatalogType operations
    CatalogType createType(CatalogType catalogType);

    boolean updateType(CatalogType catalogType);

    boolean deleteType(String code);

    CatalogType getTypeByCode(String code);

    boolean typeExistsByCode(String code);

    List<CatalogType> listAllTypes();

    // CatalogItem operations
    CatalogItem createItem(CatalogItem catalogItem);

    boolean updateItem(CatalogItem catalogItem);

    boolean deleteItem(String typeCode, String code);

    CatalogItem getItem(String typeCode, String itemCode);

    List<CatalogItem> listItemsByTypeCode(String typeCode);

    // Combined operations
    Catalog getCatalogByTypeCode(String typeCode);

    // Handle-based methods for transaction support
    CatalogType createType(Handle handle, CatalogType catalogType);

    boolean updateType(Handle handle, CatalogType catalogType);

    boolean deleteType(Handle handle, String code);

    CatalogType getTypeByCode(Handle handle, String code);

    List<CatalogType> listAllTypes(Handle handle);

    CatalogItem createItem(Handle handle, CatalogItem catalogItem);

    boolean updateItem(Handle handle, CatalogItem catalogItem);

    boolean deleteItem(Handle handle, String typeCode, String code);

    CatalogItem getItem(Handle handle, String typeCode, String itemCode);

    List<CatalogItem> listItemsByTypeCode(Handle handle, String typeCode);
}
