package com.chty.autocard.config;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@Component
public class AppConfig {

    private JSONObject config;

    private final Charset APP_CHARSET = StandardCharsets.UTF_8;

    public AppConfig() throws IOException {
        loadJsonConfig();
    }

    public void loadJsonConfig() throws IOException {
        FileInputStream fs = new FileInputStream("src/main/resources/config.json");
        this.config = JSONObject.parseObject(new String(fs.readAllBytes(), APP_CHARSET));
    }

    public JSONArray getJobs() {
        JSONArray jsonArray = config.getJSONArray("jobs");
        return jsonArray == null ? new JSONArray() : jsonArray;
    }

}