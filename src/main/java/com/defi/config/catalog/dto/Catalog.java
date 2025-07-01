package com.defi.config.catalog.dto;

import com.defi.config.catalog.entity.CatalogType;
import com.defi.config.catalog.entity.CatalogItem;
import lombok.Data;
import lombok.Builder;

import java.util.List;

@Data
@Builder
public class Catalog {
    private CatalogType type;
    private List<CatalogItem> items;
}
