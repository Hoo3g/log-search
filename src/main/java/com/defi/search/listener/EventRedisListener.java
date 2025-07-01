package com.defi.search.listener;

import com.defi.common.util.log.ErrorLogger;
import com.defi.common.util.redis.Redisson;
import com.defi.search.config.SearchConfig;
import com.defi.search.index.SearchIndexer;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RStream;
import org.redisson.api.RedissonClient;
import org.redisson.api.StreamMessageId;
import org.redisson.api.stream.StreamCreateGroupArgs;
import org.redisson.api.stream.StreamReadGroupArgs;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Data
public class EventRedisListener {
    @Getter
    private static final EventRedisListener instance = new EventRedisListener();

    private EventRedisListener() {

    }

    private String streamName;
    private String fieldName;
    private String consumerGroup;
    private String consumerName;
    private int batchSize;
    private int timeoutSeconds;

    private ExecutorService executorService;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private RedissonClient redissonClient;

    private void init() {
        loadConfig();
        this.redissonClient = Redisson.getInstance().getClient();
        this.executorService = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "redis-stream-listener");
            t.setDaemon(true);
            return t;
        });
    }

    private void loadConfig() {
        ObjectNode config = (ObjectNode) SearchConfig.getInstance().getConfig().get("event_stream");
        streamName = config.get("streamName").asText();
        fieldName = config.get("fieldName").asText();
        consumerGroup = config.get("consumerGroup").asText();
        consumerName = config.get("consumerName").asText();
        batchSize = config.get("batchSize").asInt();
        timeoutSeconds = config.get("timeoutSeconds").asInt();
    }

    public void start() {
        init();
        if (running.compareAndSet(false, true)) {
            log.info("Starting Redis Stream listener for stream: {}", streamName);
            executorService.submit(this::listenToStream);
        }
    }

    private void listenToStream() {
        RStream<String, String> stream = redissonClient.getStream(streamName);
        createConsumerGroup(stream);

        while (running.get()) {
            try {
                StreamReadGroupArgs args = StreamReadGroupArgs
                        .greaterThan(StreamMessageId.NEVER_DELIVERED)
                        .count(batchSize);
                Map<StreamMessageId, Map<String, String >> messages =
                        stream.readGroup(consumerGroup, consumerName, args);

                if (messages != null && !messages.isEmpty()) {
                    processMessages(stream, messages);
                    stream.ack(consumerGroup, messages.keySet().toArray(new StreamMessageId[0]));
                }

            } catch (Exception e) {
                ErrorLogger.create(e).log();
                sleep();
            }
        }
    }

    private void createConsumerGroup(RStream<String, String> stream) {
        try {
            if (stream.listGroups().stream().noneMatch(g -> g.getName().equals(consumerGroup))) {
                stream.createGroup(StreamCreateGroupArgs.name(consumerGroup));
            }
        } catch (Exception e) {
            ErrorLogger.create(e).log();
        }
    }


    private void processMessages(RStream<String, String> stream, Map<StreamMessageId, Map<String, String>> messages) {
        messages.forEach((messageId, fields) -> {
            try {
                String messageContent = fields.get(fieldName);
                if (messageContent != null) {
                    handleMessage(messageId, messageContent);
                    stream.ack(consumerGroup, messageId);
                }
            } catch (Exception e) {
                ErrorLogger.create(e).log();
            }
        });
    }

    private void handleMessage(StreamMessageId messageId, String messageContent) {
        SearchIndexer.getInstance().onEventLog(messageContent);
    }

    private void sleep() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void stop() {
        if (running.compareAndSet(true, false)) {
            if (executorService != null) {
                executorService.shutdown();
                try {
                    if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                        executorService.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    executorService.shutdownNow();
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}