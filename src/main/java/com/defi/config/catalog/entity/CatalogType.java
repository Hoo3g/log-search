package com.defi.config.catalog.entity;

import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.Data;

@Data
public class CatalogType {
    private String code;
    private String name;
    private ObjectNode metadata;
}