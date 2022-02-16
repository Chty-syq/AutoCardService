package com.chty.autocard.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.chty.autocard.config.AppConfig;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;

@Component
public class CheckInService {
    
    private final AppConfig appConfig;

    private final Scheduler scheduler;
    
    @Value("${app.test}")
    private boolean isTest;

    public CheckInService() throws SchedulerException, IOException {
        this.appConfig = new AppConfig();
        SchedulerFactory factory = new StdSchedulerFactory();
        this.scheduler = factory.getScheduler();
    }

    private void loadJobs() throws SchedulerException {
        JSONArray jobs = appConfig.getJobs();
        for (Object job : jobs) {
            if (job instanceof JSONObject) {
                JSONObject jobJson = (JSONObject) job;
                JobDataMap jobDataMap = new JobDataMap(jobJson);
                jobDataMap.put("isTest", isTest);
                String cron = isTest ? "0/10 * * * * ? *" : "0 0 8 * * ? *";
                this.addJob(CheckInJob.class, cron, new Date(), jobDataMap);
            }
        }
    }
    
    public void addJob(Class<? extends Job> jobClass, String cron, Date startTime, JobDataMap jobDataMap) throws SchedulerException {
        
        JobDetail jobDetail = JobBuilder.newJob(jobClass).usingJobData(jobDataMap).build();
        
        Trigger trigger = TriggerBuilder.newTrigger()
                .withSchedule(CronScheduleBuilder.cronSchedule(cron)).startAt(startTime).build();
        
        this.scheduler.scheduleJob(jobDetail, trigger);
    }
    
    public void start() throws SchedulerException {
        this.loadJobs();
        scheduler.start();
    }

}
