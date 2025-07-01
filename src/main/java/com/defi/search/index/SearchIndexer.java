package com.defi.search.index;

import com.defi.common.util.json.JsonUtil;
import com.defi.common.util.log.ErrorLogger;
import com.defi.search.config.SearchConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.opensearch.client.RestClient;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.core.IndexRequest;
import org.opensearch.client.opensearch.core.IndexResponse;
import org.opensearch.client.transport.rest_client.RestClientTransport;

import java.io.StringReader;
import java.util.UUID;

@Slf4j
public class SearchIndexer {
    @Getter
    private static final SearchIndexer instance = new SearchIndexer();

    private OpenSearchClient client;
    private String indexName;

    // Private constructor để đảm bảo là singleton
    private SearchIndexer() {}

    /**
     * Phương thức này phải được gọi khi ứng dụng khởi động
     */
    public void init() {
        try {
            log.info("Initializing SearchIndexer...");
            ObjectNode osConfig = (ObjectNode) SearchConfig.getInstance().getConfig().get("opensearch");
            String host = osConfig.get("host").asText();
            int port = osConfig.get("port").asInt();
            String scheme = osConfig.get("scheme").asText("http");
            String username = osConfig.get("username").asText(null);
            String password = osConfig.get("password").asText(null);
            this.indexName = osConfig.get("indexName").asText();

            // Cấu hình Credentials (nếu có)
            final BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            if (username != null && !username.isEmpty()) {
                credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
            }

            // Khởi tạo RestClient
            RestClient restClient = RestClient.builder(new HttpHost(host, port, scheme))
                    .setHttpClientConfigCallback(httpClientBuilder ->
                            httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider))
                    .build();

            // Khởi tạo OpenSearch Transport và Client
            RestClientTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
            this.client = new OpenSearchClient(transport);

            log.info("OpenSearch client initialized successfully. Index target: {}", this.indexName);

        } catch (Exception e) {
            log.error("Failed to initialize OpenSearch client", e);
            // Ném lỗi để ứng dụng không khởi động được nếu không kết nối được OpenSearch
            throw new RuntimeException("Could not initialize SearchIndexer", e);
        }
    }

    /**
     * Phương thức này được gọi bởi EventRedisListener mỗi khi có message mới.
     * @param data Dữ liệu dạng chuỗi JSON từ Redis Stream.
     */
    public void onEventLog(String data) {
        if (client == null) {
            log.warn("SearchIndexer is not initialized, skipping event log.");
            return;
        }

        try {
            // Parse chuỗi JSON thành JsonNode để OpenSearch client có thể xử lý
            JsonNode document = JsonUtil.toJsonObject(data);

            // Tạo một request để index tài liệu
            // Sử dụng StringReader để hiệu năng tốt hơn
            IndexRequest<JsonNode> request = new IndexRequest.Builder<JsonNode>()
                    .index(this.indexName)
                    .id(UUID.randomUUID().toString()) // Tạo ID ngẫu nhiên cho mỗi document
                    .document(document)
                    .build();

            // Thực thi request
            IndexResponse response = client.index(request);

            log.info("Document indexed successfully with ID: {} and Version: {}", response.id(), response.version());

        } catch (Exception e) {
            // Ghi log lỗi để không làm sập listener
            ErrorLogger.create(e);
        }
    }
}