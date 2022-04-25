package com.chty.autocard.config;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@Configuration
public class AppConfig {

    private JSONObject config;

    private final Charset APP_CHARSET = StandardCharsets.UTF_8;

    public void loadJsonConfig() throws IOException {
        String fileName = "src/main/resources/config.json";
        FileInputStream fs = new FileInputStream(fileName);
        this.config = JSONObject.parseObject(new String(fs.readAllBytes(), APP_CHARSET));
    }

    public JSONArray getJobs() throws IOException {
        if(this.config == null) {
            loadJsonConfig();
        }
        JSONArray jsonArray = config.getJSONArray("jobs");
        return jsonArray == null ? new JSONArray() : jsonArray;
    }

}
