package com.defi.common.util.log.appender;

import com.defi.common.util.json.JsonUtil;
import com.defi.search.SearchManager;
import com.defi.search.SearchSharedServices;
import com.defi.search.app.SearchApp;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.vertx.core.Future;

public class TestSearch {

    public static void main(String[] args) {
        System.out.println("Starting manual test for SearchManager...");

        // Bước 1: Khởi tạo các dịch vụ cần thiết
        // Giả sử SearchSharedServices.init() sẽ khởi tạo cả SearchConfig và SearchQuerier
        try {

            SearchApp app = new SearchApp();
            app.start();
        } catch (Exception e) {
            System.err.println("Failed to initialize shared services. Make sure your config is correct.");
            e.printStackTrace();
            return;
        }

        // Bước 2: Gọi trực tiếp phương thức bạn muốn test
        int size = 5;
        System.out.println("Calling searchRecentLogs with size = " + size);
        Future<ObjectNode> futureResult = SearchManager.getInstance().searchRecentLogs(size);

        // Bước 3: Xử lý kết quả bất đồng bộ
        futureResult
                .onSuccess(result -> {
                    System.out.println("\n===== TEST SUCCESSFUL =====");
                    System.out.println("Received result from OpenSearch:");
                    // In ra kết quả dạng JSON đẹp mắt
                    System.out.println(JsonUtil.toPrettyJsonString(result));
                    System.exit(0); // Kết thúc chương trình thành công
                })
                .onFailure(error -> {
                    System.err.println("\n===== TEST FAILED =====");
                    System.err.println("Error message: " + error.getMessage());
                    error.printStackTrace();
                    System.exit(1); // Kết thúc chương trình với mã lỗi
                });

        System.out.println("Request sent. Waiting for response from OpenSearch...");
    }
}