package com.chty.autocard.client;

import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class HealthReportClient{
    
    @Value("${app.health-report-client.report-url}")
    private String reportURL;

    @Value("${app.health-report-client.submit-url}")
    private String submitURL;
    
    @Getter @Setter
    private HttpClient httpClient;
    
    public List<NameValuePair> getInfo() throws URISyntaxException, IOException {
        CloseableHttpResponse response = httpClient.doGet(reportURL);
        String content = EntityUtils.toString(httpClient.getResponseContent(response));
        
        List<NameValuePair> infoList = new ArrayList<>();

        Pattern pattern = Pattern.compile("var def = (\\{.+?\\});", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(content);
        if(!matcher.find())  return null;
        JSONObject defJson = JSONObject.parseObject(matcher.group(1));
        
        pattern = Pattern.compile("\\$\\.extend\\((\\{.+?\\}), def, (\\{.+?\\})\\)", Pattern.DOTALL);
        matcher = pattern.matcher(content);
        if(!matcher.find())  return null;
        JSONObject infoJson1 = JSONObject.parseObject(matcher.group(1));
        JSONObject infoJson2 = JSONObject.parseObject(matcher.group(2));
        
        pattern = Pattern.compile("oldInfo: (\\{.+?\\}),\n");
        matcher = pattern.matcher(content);
        if(!matcher.find())  return null;
        JSONObject oldInfoJson = JSONObject.parseObject(matcher.group(1));
        
        JSONObject infoJsonAll = new JSONObject();
        infoJsonAll.putAll(defJson);
        infoJsonAll.putAll(infoJson1);
        infoJsonAll.putAll(infoJson2);
        infoJsonAll.putAll(oldInfoJson);
        
        infoJsonAll.forEach((String name, Object value) -> {
            switch (name) {
                case "date":
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
                    value = simpleDateFormat.format(new Date());
                    break;
                case "bztcyy":
                    value = "";
                    break;
            }
            if(name.equals("jrdqtlqk") && value.equals(""))  return;
            if(value == null || value.toString().equals("[]"))  return;
            infoList.add(new BasicNameValuePair(name, String.valueOf(value)));
        });
        
        return infoList;
    }
    
    public Pair<Integer,String> submit() throws URISyntaxException, IOException {
        List<NameValuePair> infoList = getInfo();
        if(infoList == null)  {
            return Pair.of(-1, "打卡信息获取失败");
        }
        
        String area = "未知";
        for(NameValuePair info: infoList) {
            if(info.getName().equals("area")) {
                area = info.getValue();
                break;
            }
        }
        
        CloseableHttpResponse response = httpClient.doPost(submitURL, infoList);
        JSONObject responseJson = JSONObject.parseObject(EntityUtils.toString(httpClient.getResponseContent(response)));
        
        return Pair.of(responseJson.getIntValue("e"), responseJson.getString("m") + "(打卡地点: " + area + ")");
    }
    
}
