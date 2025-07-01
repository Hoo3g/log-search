package com.defi.config;

import com.defi.common.util.jdbi.JdbiService;
import com.defi.config.catalog.service.CatalogService;
import com.defi.config.catalog.service.impl.CatalogServiceImpl;
import com.defi.config.eform.service.EformService;
import com.defi.config.eform.service.impl.EformServiceImpl;

public class ConfigSharedServices {

    public static void init() {
        catalogService = new CatalogServiceImpl();
        eformService = new EformServiceImpl();
        jdbiService = new JdbiService();
    }

    public static CatalogService catalogService;
    public static EformService eformService;
    public static JdbiService jdbiService;
}
