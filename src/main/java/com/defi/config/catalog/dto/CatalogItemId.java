package com.defi.config.catalog.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CatalogItemId {
    private String typeCode;
    private String code;
}
