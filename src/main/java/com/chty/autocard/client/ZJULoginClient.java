package com.chty.autocard.client;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.Consts;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

@Component
public class ZJULoginClient {
    
    @Value("${app.zju-login-client.login-url}")
    private String loginURL;
    
    @Value("${app.zju-login-client.pubkey-url}")
    private String pubkeyURL;
    
    @Getter @Setter
    private HttpClient httpClient;
    
    public boolean login(String username, String password) throws URISyntaxException, IOException {
        return login(username, password, null, false) != null;
    }
    
    public String login(String username, String password, String targetService, boolean URLEncode) throws URISyntaxException, IOException {
        String targetURL = loginURL;
        if(targetService != null) {
            targetURL += "?service=" + (URLEncode ? URLEncoder.encode(targetService, Consts.UTF_8) : targetService);
        }
        
        Pair<Integer,String> execution = getExecution();
        switch (execution.getLeft()) {
            case  0:  break;
            case -1:  return null;
            case  1:  return EntityUtils.toString(httpClient.getResponseContent(httpClient.doGet(targetURL)));
        }

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("username", username));
        params.add(new BasicNameValuePair("password", password));
        params.add(new BasicNameValuePair("_eventId", "submit"));
        params.add(new BasicNameValuePair("authcode", ""));
        params.add(new BasicNameValuePair("execution", execution.getRight()));
        
        CloseableHttpResponse response = httpClient.doPost(loginURL, params);
        
        if(response.getStatusLine().getStatusCode() == 302) {
            return EntityUtils.toString(httpClient.getResponseContent(response));
        }
        
        return null;
    }
    
    public Pair<Integer,String> getExecution() {
        try {
            httpClient.setHttpClient(false, 0);

            CloseableHttpResponse response = httpClient.doGet(loginURL);
            int status = response.getStatusLine().getStatusCode();
            if(status == 302) {
                return Pair.of(1, null);
            } else {
                String content = EntityUtils.toString(httpClient.getResponseContent(response));
                Document document = Jsoup.parse(content);  //parse html
                return Pair.of(0, document.getElementsByAttributeValue("name", "execution").val());
            }
        } catch (Exception e) {
            return Pair.of(-1, null);
        } finally {
            httpClient.setHttpClient(true, 10);
        }
    }
    
}
