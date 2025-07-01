package com.defi.search.app;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import com.defi.common.mode.ModeManager;
import com.defi.common.permission.PermissionChecker;
import com.defi.common.token.TokenManager;
import com.defi.common.util.file.FileUtil;
import com.defi.common.util.flyway.FlywayMigrator;
import com.defi.common.util.jdbi.JdbiProvider;
import com.defi.common.util.json.JsonUtil;
import com.defi.common.util.redis.Redisson;
import com.defi.common.util.sql.HikariClient;
import com.defi.common.vertx.VertxServer;
import com.defi.config.ConfigSharedServices;
import com.defi.config.vertx.ConfigVerticle;
import com.defi.search.config.SearchConfig;
import com.defi.search.index.SearchIndexer;
import com.defi.search.listener.EventRedisListener;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;

@Slf4j
public class SearchApp {
    public void start() {
        try {
            initLogic();
            initServices();
            startLoop();
            startHttpServer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startServiceOnly() {
        try {
            initLogic();
            initServices();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initServices() {
        ConfigSharedServices.init();
        EventRedisListener.getInstance().start();
        SearchIndexer.getInstance().init();
    }

    private void startHttpServer() {
        String configFile = ModeManager.getInstance().getRealConfigFilePath("vertx/vertx.json");
        ObjectNode config = JsonUtil.toJsonObject(FileUtil.readString(configFile));
        VertxServer.getInstance().start(config, ConfigVerticle.class);
    }

    private void startLoop() {
        ScheduledExecutorService scheduler = newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
        }, 0, 30, TimeUnit.SECONDS);
    }

    private void initLogic() throws Exception {
        ModeManager.getInstance().init("lan");
        SearchConfig.getInstance().init("search/search.json");
        initLogBack();
        initHikariClient();
        initRedisson();
        initPermission();
        String publicKey = ModeManager.getInstance()
                .getConfigContent("jwt/public_key.pem");
        String privateKey = ModeManager.getInstance()
                .getConfigContent("jwt/private_key.pem");
        TokenManager.getInstance().init(publicKey, privateKey, "");

    }

    private void initPermission() {
        String casbinConfig = ModeManager.getInstance().getConfigContent("casbin/casbin.json");
        ObjectNode config = JsonUtil.toJsonObject(casbinConfig);

        String casbinModelFile = config.get("casbinModelFile").asText();
        String modelConfig = ModeManager.getInstance().getConfigContent(casbinModelFile);

        String policySourceType = config.get("policySourceType").asText();
        ObjectNode policySource = (ObjectNode) config.get("policySource");
        String policySourceQuery = policySource.get(policySourceType).asText();

        String versionSourceType = config.get("versionSourceType").asText();
        ObjectNode versionSource = (ObjectNode) config.get("versionSource");
        String versionSourceQuery = versionSource.get(versionSourceType).asText();

        ObjectNode polling = (ObjectNode) config.get("polling");
        boolean pollingEnabled = polling.get("enabled").asBoolean();
        int durationSeconds = polling.get("interval").asInt();

        DataSource dataSource = HikariClient.getInstance().getDataSource();
        // Get resources from config
        JsonNode resourcesNode = config.get("resources");
        List<String> resources = List.of();
        if (resourcesNode != null && resourcesNode.isArray()) {
            resources = new ArrayList<>();
            for (JsonNode resource : resourcesNode) {
                resources.add(resource.asText());
            }
        }

        PermissionChecker.getInstance().init(
                modelConfig,
                policySourceType,
                policySourceQuery,
                resources,
                pollingEnabled,
                versionSourceType,
                versionSourceQuery,
                durationSeconds,
                dataSource);
    }

    private void initLogBack() throws Exception {
        String logConfigFile = ModeManager.getInstance().getRealConfigFilePath("log/logback.xml");
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        File configFile = new File(logConfigFile);
        context.reset();
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(context);
        configurator.doConfigure(configFile);
    }

    private void initRedisson() throws IOException {
        String yamlConfig = ModeManager.getInstance().getConfigContent("redis/redis-single.yaml");
        Redisson.getInstance().init(yamlConfig);
    }

    private void initHikariClient() throws IOException {
        String filePath = ModeManager.getInstance().getRealConfigFilePath("sql/hikari.properties");
        Properties prop = new Properties();
        try (FileInputStream fis = new FileInputStream(filePath)) {
            prop.load(fis);
        }
        HikariClient.getInstance().init(prop);
        DataSource dataSource = HikariClient.getInstance().getDataSource();
        FlywayMigrator.migrate(dataSource);
        JdbiProvider.getInstance().init(dataSource);
    }
}
