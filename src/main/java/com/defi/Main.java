package com.defi;

import com.defi.common.token.entity.SubjectType;
import com.defi.common.util.json.JsonUtil;
import com.defi.common.util.log.EventLogger;
import com.defi.common.util.log.entity.EventLog;
import com.defi.common.util.string.RandomStringUtil;
import com.defi.search.app.SearchApp;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Main {
    public static void main(String[] args) {
        try {
            log.info("Starting SearchApp...");
            SearchApp app = new SearchApp();
            app.start();
            EventLog eventLog = EventLog.builder()
                    .id(RandomStringUtil.uuidV7().toString())
                    .subjectType(SubjectType.SYSTEM)
                    .subjectId("333-subject-id")
                    .targetType("test")
                    .targetId("333-target-id")
                    .data(JsonUtil.createObjectNode().put("key", "value"))
                    .correlationId("test")
                    .createdAt(System.currentTimeMillis())
                    .build();
            EventLogger.log(eventLog);
            log.info("SearchApp started successfully.");
        } catch (Exception e) {
            log.error("", e);
        }
    }
}
