package com.defi;

import com.defi.common.token.entity.SubjectType;
import com.defi.common.util.log.EventLogger;
import com.defi.common.util.log.entity.EventLog;
import com.defi.search.app.SearchApp;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.javalin.Javalin;
import io.javalin.http.Context;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.concurrent.*;

@Slf4j
public class Main {

    private static final int API_PORT = 8081;
    private static final String ALLOWED_ORIGIN = "http://localhost:3001";
    private static final ObjectMapper mapper = new ObjectMapper();

    private static final int NUM_CONSUMERS = 4; // Số consumer threads
    private static final BlockingQueue<EventLog> logQueue = new LinkedBlockingQueue<>(10000);
    private static final ExecutorService logExecutor = Executors.newFixedThreadPool(NUM_CONSUMERS);

    public static void main(String[] args) {
        try {
            log.info("Starting SearchApp...");
            SearchApp app = new SearchApp();
            app.start();
            log.info("SearchApp started successfully");

            startLogConsumers();

            Javalin api = Javalin.create(config -> {
                config.plugins.enableCors(cors -> cors.add(it -> it.allowHost(ALLOWED_ORIGIN)));
            }).start(API_PORT);

            log.info("Javalin server started on port {}", API_PORT);

            api.post("/api/log", Main::handleLogRequest);
            api.get("/health", ctx -> ctx.status(200).result("OK"));
            api.post("/test-log", Main::handleTestLog);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                log.info("Shutting down server...");
                logExecutor.shutdown();
                try {
                    if (!logExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                        logExecutor.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    logExecutor.shutdownNow();
                }
                api.stop();
                log.info("SearchApp stopped gracefully");
            }));

        } catch (Exception e) {
            log.error("Failed to start SearchApp", e);
        }
    }

    /**
     * Khởi động nhiều consumer threads.
     */
    private static void startLogConsumers() {
        for (int i = 0; i < NUM_CONSUMERS; i++) {
            final int consumerId = i + 1;
            logExecutor.submit(() -> {
                log.info("Log consumer #{} started", consumerId);
                while (true) {
                    try {
                        EventLog logItem = logQueue.take(); // Blocking take
                        EventLogger.log(logItem);
                        log.info("[Consumer #{}] Log written: {}", consumerId, logItem.getId());
                    } catch (Exception e) {
                        log.error("Error in log consumer #{}", consumerId, e);
                    }
                }
            });
        }
    }

    private static void handleLogRequest(Context ctx) {
        log.info("Received POST /api/log request");
        try {
            ObjectNode json = (ObjectNode) mapper.readTree(ctx.body());
            EventLog eventLog = buildEventLogFromJson(json);

            // Put log vào queue (blocking để không mất log)
            logQueue.put(eventLog);
            ctx.status(200).result("Queued successfully: " + eventLog.getId());
        } catch (Exception e) {
            log.error("Error processing /api/log request", e);
            ctx.status(500).result("Error: " + e.getMessage());
        }
    }

    private static EventLog buildEventLogFromJson(ObjectNode json) {
        String id = getTextField(json, "id");
        SubjectType subjectType = SubjectType.valueOf(getTextField(json, "subjectType"));
        String subjectId = getTextField(json, "subjectId");
        String targetType = getTextField(json, "targetType");
        String targetId = getTextField(json, "targetId");
        String correlationId = getTextField(json, "correlationId");
        long createdAt = getLongField(json, "createdAt");
        ObjectNode data = Optional.ofNullable((ObjectNode) json.get("data")).orElse(mapper.createObjectNode());

        return EventLog.builder()
                .id(id)
                .subjectType(subjectType)
                .subjectId(subjectId)
                .targetType(targetType)
                .targetId(targetId)
                .correlationId(correlationId)
                .createdAt(createdAt)
                .data(data)
                .build();
    }

    private static String getTextField(ObjectNode json, String fieldName) {
        return Optional.ofNullable(json.get(fieldName))
                .map(node -> node.asText())
                .orElseThrow(() -> new IllegalArgumentException("Missing or invalid field: " + fieldName));
    }

    private static long getLongField(ObjectNode json, String fieldName) {
        return Optional.ofNullable(json.get(fieldName))
                .map(node -> {
                    if (node.canConvertToLong()) {
                        return node.asLong();
                    } else {
                        throw new IllegalArgumentException("Field " + fieldName + " is not a valid long");
                    }
                })
                .orElseThrow(() -> new IllegalArgumentException("Missing field: " + fieldName));
    }

    private static void handleTestLog(Context ctx) {
        log.info("Received POST /test-log request");
        try {
            EventLog testLog = EventLog.builder()
                    .id("test-" + System.currentTimeMillis())
                    .subjectType(SubjectType.USER)
                    .subjectId("test-user")
                    .targetType("test")
                    .targetId("test-target")
                    .data(mapper.createObjectNode().put("test", "data"))
                    .correlationId("test-correlation")
                    .createdAt(System.currentTimeMillis())
                    .build();

            EventLogger.log(testLog);
            ctx.status(200).result("Test log successful: " + testLog.getId());
        } catch (Exception e) {
            log.error("Error during test log", e);
            ctx.status(500).result("Test failed: " + e.getMessage());
        }
    }
}
