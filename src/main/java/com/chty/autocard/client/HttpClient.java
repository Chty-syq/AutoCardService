package com.chty.autocard.client;

import com.chty.autocard.utils.AppUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.List;


public class HttpClient implements Closeable {
    
    private String cookieFile;
    
    private boolean enableRedirect;
    
    private CookieStore cookieStore;
    
    private CloseableHttpClient httpClient;

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:89.0) Gecko/20100101 Firefox/89.0";

    public HttpClient() {
        this("cookie.cache");
    }
    
    public HttpClient(String cookieFile) {
        this.cookieFile = cookieFile;
        initCookieStore();
        setHttpClient(true, 10);
    }
    
    public void setHttpClient(boolean enableRedirect, int maxRedirect) {
        try{
            this.enableRedirect = enableRedirect;

            RequestConfig config = RequestConfig.custom().setCircularRedirectsAllowed(true)
                    .setMaxRedirects(maxRedirect).setRedirectsEnabled(enableRedirect).build();

            if(cookieStore == null)  initCookieStore();
            if(httpClient != null)  httpClient.close();
            this.httpClient = HttpClients.custom().setDefaultCookieStore(this.cookieStore)
                    .setDefaultRequestConfig(config).setUserAgent(USER_AGENT).build();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public CloseableHttpResponse doGet(String url, Header... headers) throws URISyntaxException, IOException {
        return doGet(url, null, headers);
    }
    
    public CloseableHttpResponse doGet(String url, List<NameValuePair> params, Header... headers) throws URISyntaxException, IOException {
        URIBuilder uriBuilder = new URIBuilder(url, StandardCharsets.UTF_8);
        if(params != null) {
            uriBuilder.setParameters(params);
        }
        HttpUriRequest request = new HttpGet(uriBuilder.build());
        request.setHeaders(headers);
        return httpClient.execute(request);
    }
    
    public CloseableHttpResponse doPost(String url, Header... headers) throws IOException {
        return doPost(url, null, headers);
    }
    
    public CloseableHttpResponse doPost(String url, List<NameValuePair> params, Header... headers) throws IOException {
        HttpPost request = new HttpPost(url);
        if(params != null) {
            request.setEntity(new UrlEncodedFormEntity(params, StandardCharsets.UTF_8));
        }
        request.setHeaders(headers);
        return httpClient.execute(request);
    }
    
    public CloseableHttpResponse isSuccess(CloseableHttpResponse response) throws URISyntaxException, IOException {
        if(response == null)  return null;
        if(response.getStatusLine().getStatusCode() < 400) {
            return doRedirects(response);
        }
        return null;
    }
    
    public HttpEntity getResponseContent(CloseableHttpResponse response) throws URISyntaxException, IOException {
        response = isSuccess(response);
        if(response != null) {
            return response.getEntity();
        }
        return null;
    }
    
    private void initCookieStore() {
        if(cookieFile == null) {
            this.cookieStore = new BasicCookieStore();
            return;
        }
        try {
            String workDir = AppUtils.getWorkDir();
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File(workDir, this.cookieFile)));
            this.cookieStore = (CookieStore) ois.readObject();
        } catch (Exception e) {
            this.cookieStore = new BasicCookieStore();
        }
    }
    
    private CloseableHttpResponse doRedirects(CloseableHttpResponse response) throws URISyntaxException, IOException {
        if(!this.enableRedirect || response == null)  return response;
        int statusCode = response.getStatusLine().getStatusCode();
        if(statusCode == 302) {
            String url = response.getFirstHeader("Location").getValue();
            if(url != null) {
                response.close();
                return doRedirects(doGet(url));
            }
        }
        return response;
    }
    
    @Override
    public void close() throws IOException {
        
    }
}
