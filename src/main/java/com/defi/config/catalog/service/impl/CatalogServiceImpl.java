package com.defi.config.catalog.service.impl;

import com.defi.common.util.jdbi.JdbiProvider;
import com.defi.config.catalog.dto.Catalog;
import com.defi.config.catalog.entity.CatalogType;
import com.defi.config.catalog.entity.CatalogItem;
import com.defi.config.catalog.service.CatalogService;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.BeanMapper;

import java.util.List;

@Slf4j
public class CatalogServiceImpl implements CatalogService {

    private final Jdbi jdbi;

    public CatalogServiceImpl() {
        this.jdbi = JdbiProvider.getInstance().getJdbi();
    }

    // CatalogType operations
    @Override
    public CatalogType createType(CatalogType catalogType) {
        return jdbi.inTransaction(handle -> createType(handle, catalogType));
    }

    @Override
    public boolean updateType(CatalogType catalogType) {
        return jdbi.inTransaction(handle -> updateType(handle, catalogType));
    }

    @Override
    public boolean deleteType(String code) {
        return jdbi.inTransaction(handle -> deleteType(handle, code));
    }

    @Override
    public CatalogType getTypeByCode(String code) {
        return jdbi.withHandle(handle -> getTypeByCode(handle, code));
    }

    @Override
    public boolean typeExistsByCode(String code) {
        return jdbi.withHandle(handle -> handle.createQuery("SELECT 1 FROM catalog_types WHERE code = :code LIMIT 1")
                .bind("code", code)
                .mapTo(Integer.class)
                .findOne()
                .isPresent());
    }

    @Override
    public List<CatalogType> listAllTypes() {
        return jdbi.withHandle(this::listAllTypes);
    }

    // CatalogItem operations
    @Override
    public CatalogItem createItem(CatalogItem catalogItem) {
        return jdbi.inTransaction(handle -> createItem(handle, catalogItem));
    }

    @Override
    public boolean updateItem(CatalogItem catalogItem) {
        return jdbi.inTransaction(handle -> updateItem(handle, catalogItem));
    }

    @Override
    public boolean deleteItem(String typeCode, String code) {
        return jdbi.inTransaction(handle -> deleteItem(handle, typeCode, code));
    }

    @Override
    public CatalogItem getItem(String typeCode, String itemCode) {
        return jdbi.withHandle(handle -> getItem(handle, typeCode, itemCode));
    }

    @Override
    public List<CatalogItem> listItemsByTypeCode(String typeCode) {
        return jdbi.withHandle(handle -> listItemsByTypeCode(handle, typeCode));
    }

    // Combined operations
    @Override
    public Catalog getCatalogByTypeCode(String typeCode) {
        return jdbi.withHandle(handle -> {
            CatalogType type = getTypeByCode(handle, typeCode);
            if (type == null) {
                return null;
            }
            List<CatalogItem> items = listItemsByTypeCode(handle, typeCode);
            return Catalog.builder()
                    .type(type)
                    .items(items)
                    .build();
        });
    }

    // Handle-based methods for transaction support
    @Override
    public CatalogType createType(Handle handle, CatalogType catalogType) {
        handle.createUpdate(
                "INSERT INTO catalog_types (code, name, metadata) " +
                        "VALUES (:code, :name, :metadata::jsonb)")
                .bindBean(catalogType)
                .execute();
        return catalogType;
    }

    @Override
    public boolean updateType(Handle handle, CatalogType catalogType) {
        int affectedRows = handle.createUpdate(
                "UPDATE catalog_types SET " +
                        "name = :name, " +
                        "metadata = :metadata::jsonb " +
                        "WHERE code = :code")
                .bindBean(catalogType)
                .execute();
        return affectedRows > 0;
    }

    @Override
    public boolean deleteType(Handle handle, String code) {
        int affectedRows = handle.createUpdate("DELETE FROM catalog_types WHERE code = :code")
                .bind("code", code)
                .execute();
        return affectedRows > 0;
    }

    @Override
    public CatalogType getTypeByCode(Handle handle, String code) {
        return handle.createQuery("SELECT * FROM catalog_types WHERE code = :code")
                .bind("code", code)
                .map(BeanMapper.of(CatalogType.class))
                .findOne()
                .orElse(null);
    }

    @Override
    public List<CatalogType> listAllTypes(Handle handle) {
        return handle.createQuery("SELECT * FROM catalog_types ORDER BY code")
                .map(BeanMapper.of(CatalogType.class))
                .list();
    }

    @Override
    public CatalogItem createItem(Handle handle, CatalogItem catalogItem) {
        handle.createUpdate(
                "INSERT INTO catalog_items (type_code, code, name, metadata) " +
                        "VALUES (:typeCode, :code, :name, :metadata::jsonb)")
                .bindBean(catalogItem)
                .execute();
        return catalogItem;
    }

    @Override
    public boolean updateItem(Handle handle, CatalogItem catalogItem) {
        int affectedRows = handle.createUpdate(
                "UPDATE catalog_items SET " +
                        "name = :name, " +
                        "metadata = :metadata::jsonb " +
                        "WHERE type_code = :typeCode AND code = :code")
                .bindBean(catalogItem)
                .execute();
        return affectedRows > 0;
    }

    @Override
    public boolean deleteItem(Handle handle, String typeCode, String code) {
        int affectedRows = handle.createUpdate("DELETE FROM catalog_items WHERE type_code = :typeCode AND code = :code")
                .bind("typeCode", typeCode)
                .bind("code", code)
                .execute();
        return affectedRows > 0;
    }

    @Override
    public CatalogItem getItem(Handle handle, String typeCode, String itemCode) {
        return handle.createQuery("SELECT * FROM catalog_items WHERE type_code = :typeCode AND code = :itemCode")
                .bind("typeCode", typeCode)
                .bind("itemCode", itemCode)
                .map(BeanMapper.of(CatalogItem.class))
                .findOne()
                .orElse(null);
    }

    @Override
    public List<CatalogItem> listItemsByTypeCode(Handle handle, String typeCode) {
        return handle.createQuery("SELECT * FROM catalog_items WHERE type_code = :typeCode ORDER BY code")
                .bind("typeCode", typeCode)
                .map(BeanMapper.of(CatalogItem.class))
                .list();
    }
}
