package com.defi.config.orchestrator.event;

import com.defi.common.util.log.entity.EventLog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfigEventContext<T> {
    private EventLog event;
    private T context;
}