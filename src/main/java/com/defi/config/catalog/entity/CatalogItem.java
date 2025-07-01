package com.defi.config.catalog.entity;

import lombok.Data;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Data
public class CatalogItem {
    private String typeCode;
    private String code;
    private String name;
    private ObjectNode metadata;
}
