package com.defi.config.catalog.cache;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CatalogCacheManager {
    private static final CatalogCacheManager instance = new CatalogCacheManager();

    private CatalogCacheManager() {
        // private constructor for singleton
    }
}