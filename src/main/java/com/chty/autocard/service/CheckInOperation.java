package com.chty.autocard.service;

import com.chty.autocard.client.EmailClient;
import com.chty.autocard.client.HealthReportClient;
import com.chty.autocard.client.HttpClient;
import com.chty.autocard.client.ZJULoginClient;
import com.chty.autocard.config.AppConfig;
import com.chty.autocard.utils.LoggerUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.quartz.JobDataMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class CheckInOperation {

    @Autowired
    private AppConfig appConfig;

    @Autowired
    private ZJULoginClient loginClient;

    @Autowired
    private HealthReportClient reportClient;

    @Autowired
    private EmailClient emailClient;

    @Value("${app.mode}")
    private Integer mode;

    void enact(JobDataMap dataMap) throws IOException, InterruptedException {
        boolean delay = dataMap.getBooleanValue("delay");
        String username = dataMap.getString("username");
        String password = dataMap.getString("password");
        String email = dataMap.getString("email");

        if(delay && mode == 0) {
            long seconds = (long) (Math.random() * 1800);
            Thread.sleep(seconds * 1000);
        }

        Pair<Boolean, String> result = Pair.of(false, "打卡失败");
        int retryNum = 5;
        for(int i = 0; i < retryNum; ++i) {
            result = this.AutoCard(username, password, email);
            LoggerUtils.info(username + ": " + result.getRight());
            if(result.getLeft()) break;
            long seconds = (long) (Math.random() * 30);
            Thread.sleep(seconds * 1000);
        }
        
        if(mode == 0 || mode == 1) {
            try {
                emailClient.send(email, "健康打卡通知", result.getRight());
                LoggerUtils.info(username + ": 邮件发送成功");
            } catch (Exception e) {
                LoggerUtils.info(username + ": 邮件发送失败");
            }
        }
    }

    private Pair<Boolean, String> AutoCard(String username, String password, String email) {

        if(loginClient == null || reportClient == null || emailClient == null) return Pair.of(false, "系统错误");

        HttpClient httpClient = new HttpClient();

        loginClient.setHttpClient(httpClient);
        reportClient.setHttpClient(httpClient);

        try {
            boolean success = loginClient.login(username, password);
            if(!success) {
                return Pair.of(false, username + ": 登录失败");
            }
        } catch (Exception e) {
            return Pair.of(false, username + ": 登录失败");
        }

        try {
            Pair<Integer,String> result = reportClient.submit();
            if(result.getLeft() == -1) {
                return Pair.of(false, username + ": " + result.getRight());
            }
            if(result.getRight().contains("验证码错误")) {
                return Pair.of(false, username + ": " + result.getRight());
            }
            return Pair.of(true, username + ": " + result.getRight());
        } catch (Exception e) {
            return Pair.of(false, username + ": 打卡失败");
        }
    }
}
