package com.defi.search.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TargetCount {
    private String targetId;
    private String targetType;
    private long eventCount;

    public TargetCount(String key, long l) {
    }
}
