package com.defi.common.util.log.appender;

import com.defi.common.token.entity.SubjectType;
import com.defi.common.util.json.JsonUtil;
import com.defi.common.util.log.EventLogger;
import com.defi.common.util.log.entity.EventLog;
import com.defi.common.util.string.RandomStringUtil;
import com.defi.search.app.SearchApp;

public class EventRedisAppenderTest {
    public static void main(String [] args) {
        try {
            SearchApp searchApp = new SearchApp();
            searchApp.startServiceOnly();
            EventLog eventLog = EventLog.builder()
                    .id(RandomStringUtil.uuidV7().toString())
                    .subjectType(SubjectType.SYSTEM)
                    .subjectId("test-subject-id")
                    .targetType("test")
                    .targetId("test-target-id")
                    .data(JsonUtil.createObjectNode().put("key", "value"))
                    .correlationId("test")
                    .createdAt(System.currentTimeMillis())
                    .build();
            EventLogger.log(eventLog);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
