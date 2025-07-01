package com.defi.config.eform.dto;

import com.defi.common.util.filter.SortOrder;
import lombok.Data;

@Data
public class EformFilter {
    private String keyword;
    private Integer page = 0;
    private Integer size = 10;
    private String sortBy = "code";
    private SortOrder sortOrder = SortOrder.ASC;

    /**
     * Validates the filter parameters
     */
    public boolean isValid() {
        // Page must be non-negative
        if (page != null && page < 0) {
            return false;
        }

        // Size must be positive and reasonable
        if (size != null && (size <= 0 || size > 100)) {
            return false;
        }
        return true;
    }
}