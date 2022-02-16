package com.chty.autocard.service;

import com.chty.autocard.client.EmailClient;
import com.chty.autocard.client.HealthReportClient;
import com.chty.autocard.client.HttpClient;
import com.chty.autocard.client.ZJULoginClient;
import com.chty.autocard.utils.AppUtils;
import com.chty.autocard.utils.LoggerUtils;
import lombok.SneakyThrows;
import org.apache.commons.lang3.tuple.Pair;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class CheckInJob implements Job {
    
    private final ZJULoginClient loginClient = AppUtils.getBean(ZJULoginClient.class);
    
    private final HealthReportClient reportClient = AppUtils.getBean(HealthReportClient.class);
    
    private final EmailClient emailClient = AppUtils.getBean(EmailClient.class);
    
    @SneakyThrows
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        if(loginClient == null || reportClient == null || emailClient == null)  return;
        
        //SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        //System.out.println(sdf.format(new Date()));
        
        JobDataMap dataMap = context.getMergedJobDataMap();
        boolean delay = dataMap.getBooleanValue("delay");
        boolean isTest = dataMap.getBooleanValue("isTest");
        String username = dataMap.getString("username");
        String password = dataMap.getString("password");
        String email = dataMap.getString("email");
        
        if(delay && !isTest) {
            long seconds = (long) (Math.random() * 1800);
            Thread.sleep(seconds * 1000);
        }
    
        HttpClient httpClient = new HttpClient();
        
        loginClient.setHttpClient(httpClient);
        reportClient.setHttpClient(httpClient);
        
        boolean success = loginClient.login(username, password);
        LoggerUtils.info(username + ": 登录" + (success ? "成功" : "失败"));
        
        Pair<Integer,String> result = reportClient.submit();
        LoggerUtils.info(username + ": " + result.getRight());
        
        emailClient.send(email, "健康打卡通知", result.getRight());
        LoggerUtils.info(username + ": 邮件发送成功");
    }
    
}
