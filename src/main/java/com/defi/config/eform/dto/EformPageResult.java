package com.defi.config.eform.dto;

import com.defi.config.eform.entity.Eform;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class EformPageResult {
    private List<Eform> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean hasNext;
    private boolean hasPrevious;
}