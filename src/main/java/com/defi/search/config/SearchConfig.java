package com.defi.search.config;

import com.defi.common.mode.ModeManager;
import com.defi.common.util.file.FileUtil;
import com.defi.common.util.json.JsonUtil;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;

public class SearchConfig {
    @Getter
    private ObjectNode config;
    @Getter
    private static final SearchConfig instance = new SearchConfig();

    private SearchConfig() {

    }

    public void init(String configFile){
        String realPathFile = ModeManager.getInstance().getRealConfigFilePath(configFile);
        String data = FileUtil.readString(realPathFile);
        this.config = JsonUtil.toJsonObject(data);
    }
}
