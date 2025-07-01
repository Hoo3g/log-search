package com.defi.search;

import com.defi.common.util.jdbi.JdbiService;
import com.defi.search.SearchQuerier;

public class SearchSharedServices {

    public static JdbiService jdbiService;

    public static void init() {
        if (jdbiService == null)
            jdbiService = new JdbiService();

        SearchQuerier.getInstance().init();
    }
}