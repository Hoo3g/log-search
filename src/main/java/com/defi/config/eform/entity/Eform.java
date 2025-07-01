package com.defi.config.eform.entity;

import lombok.Data;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Data
public class Eform {
    private String code;
    private String name;
    private ObjectNode uiConfig;
    private ObjectNode jsonSchema;
}