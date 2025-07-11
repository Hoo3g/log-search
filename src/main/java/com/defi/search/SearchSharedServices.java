package com.defi.search;

import com.defi.common.util.jdbi.JdbiService;
import com.defi.search.SearchQuerier;
import com.defi.search.service.impl.SearchLogImpl;

public class SearchSharedServices {

    public static JdbiService jdbiService;

    public static void init() {
        if (jdbiService == null)
            jdbiService = new JdbiService();

        SearchQuerier.getInstance().init();
        SearchLogImpl.getInstance().init();
    }
}