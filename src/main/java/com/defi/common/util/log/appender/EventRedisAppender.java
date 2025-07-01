package com.defi.common.util.log.appender;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import com.defi.common.util.log.ErrorLogger;
import com.defi.common.util.redis.Redisson;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RStream;
import org.redisson.api.RedissonClient;
import org.redisson.api.stream.StreamAddArgs;

@Slf4j
@Setter
public class EventRedisAppender extends AppenderBase<ILoggingEvent> {

    private String streamName;
    private String fieldName;

    @Override
    protected void append(ILoggingEvent eventObject) {
        try {
            RedissonClient client = Redisson.getInstance().getClient();
            RStream<String, String> stream = client.getStream(streamName);
            String message = eventObject.getMessage();
            stream.add(StreamAddArgs.entry(fieldName, message));
        } catch (Exception e) {
            ErrorLogger.create(e).log();
        }
    }
}