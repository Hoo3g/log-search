package com.defi.config.eform.service.impl;

import com.defi.common.util.filter.SortOrder;
import com.defi.common.util.jdbi.JdbiProvider;
import com.defi.config.eform.dto.EformFilter;
import com.defi.config.eform.dto.EformPageResult;
import com.defi.config.eform.entity.Eform;
import com.defi.config.eform.service.EformService;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.BeanMapper;

import java.util.List;

@Slf4j
public class EformServiceImpl implements EformService {

    private final Jdbi jdbi;

    public EformServiceImpl() {
        this.jdbi = JdbiProvider.getInstance().getJdbi();
    }

    @Override
    public Eform create(Eform eform) {
        return jdbi.inTransaction(handle -> create(handle, eform));
    }

    @Override
    public boolean update(Eform eform) {
        return jdbi.inTransaction(handle -> update(handle, eform));
    }

    @Override
    public boolean delete(String code) {
        return jdbi.inTransaction(handle -> delete(handle, code));
    }

    @Override
    public Eform getByCode(String code) {
        return jdbi.withHandle(handle -> getByCode(handle, code));
    }

    @Override
    public boolean existsByCode(String code) {
        return jdbi.withHandle(handle -> handle.createQuery("SELECT 1 FROM eforms WHERE code = :code LIMIT 1")
                .bind("code", code)
                .mapTo(Integer.class)
                .findOne()
                .isPresent());
    }

    @Override
    public List<Eform> listAll() {
        return jdbi.withHandle(this::listAll);
    }

    @Override
    public EformPageResult filter(EformFilter filter) {
        return jdbi.withHandle(handle -> filter(handle, filter));
    }

    // Handle-based methods for transaction support
    @Override
    public Eform create(Handle handle, Eform eform) {
        handle.createUpdate(
                "INSERT INTO eforms (code, name, uiConfig, jsonSchema) " +
                        "VALUES (:code, :name, :uiConfig::jsonb, :jsonSchema::jsonb)")
                .bindBean(eform)
                .execute();
        return eform;
    }

    @Override
    public boolean update(Handle handle, Eform eform) {
        int affectedRows = handle.createUpdate(
                "UPDATE eforms SET " +
                        "name = :name, " +
                        "uiConfig = :uiConfig::jsonb, " +
                        "jsonSchema = :jsonSchema::jsonb " +
                        "WHERE code = :code")
                .bindBean(eform)
                .execute();
        return affectedRows > 0;
    }

    @Override
    public boolean delete(Handle handle, String code) {
        int affectedRows = handle.createUpdate("DELETE FROM eforms WHERE code = :code")
                .bind("code", code)
                .execute();
        return affectedRows > 0;
    }

    @Override
    public Eform getByCode(Handle handle, String code) {
        return handle.createQuery("SELECT * FROM eforms WHERE code = :code")
                .bind("code", code)
                .map(BeanMapper.of(Eform.class))
                .findOne()
                .orElse(null);
    }

    @Override
    public boolean existsByCode(Handle handle, String code) {
        return handle.createQuery("SELECT 1 FROM eforms WHERE code = :code LIMIT 1")
                .bind("code", code)
                .mapTo(Integer.class)
                .findOne()
                .isPresent();
    }

    @Override
    public List<Eform> listAll(Handle handle) {
        return handle.createQuery("SELECT * FROM eforms ORDER BY code")
                .map(BeanMapper.of(Eform.class))
                .list();
    }

    @Override
    public EformPageResult filter(Handle handle, EformFilter filter) {
        // Build WHERE clause
        StringBuilder whereClause = new StringBuilder();
        if (filter.getKeyword() != null && !filter.getKeyword().trim().isEmpty()) {
            whereClause.append(" WHERE (immutable_unaccent(lower(code)) ILIKE immutable_unaccent(lower(:keyword)) ")
                    .append(" OR immutable_unaccent(lower(name)) ILIKE immutable_unaccent(lower(:keyword)))");
        }

        // Build ORDER BY clause
        String orderBy = "ORDER BY " + buildOrderBy(filter.getSortBy(), filter.getSortOrder());

        // Count total records
        String countQuery = "SELECT COUNT(*) FROM eforms" + whereClause.toString();
        long totalElements = handle.createQuery(countQuery)
                .bind("keyword", filter.getKeyword() != null ? "%" + filter.getKeyword().trim() + "%" : null)
                .mapTo(Long.class)
                .one();

        // Get paginated results
        String selectQuery = "SELECT * FROM eforms" + whereClause.toString() + " " + orderBy
                + " LIMIT :size OFFSET :offset";

        List<Eform> content = handle.createQuery(selectQuery)
                .bind("keyword", filter.getKeyword() != null ? "%" + filter.getKeyword().trim() + "%" : null)
                .bind("size", filter.getSize())
                .bind("offset", filter.getPage() * filter.getSize())
                .map(BeanMapper.of(Eform.class))
                .list();

        // Calculate pagination info
        int totalPages = (int) Math.ceil((double) totalElements / filter.getSize());
        boolean hasNext = filter.getPage() < totalPages - 1;
        boolean hasPrevious = filter.getPage() > 0;

        return EformPageResult.builder()
                .content(content)
                .page(filter.getPage())
                .size(filter.getSize())
                .totalElements(totalElements)
                .totalPages(totalPages)
                .hasNext(hasNext)
                .hasPrevious(hasPrevious)
                .build();
    }

    private String buildOrderBy(String sortBy, SortOrder sortOrder) {
        // Validate sortBy field
        String validSortBy = switch (sortBy) {
            case "code", "name" -> sortBy;
            default -> "code";
        };

        String direction = sortOrder == SortOrder.DESC ? "DESC" : "ASC";
        return validSortBy + " " + direction;
    }
}