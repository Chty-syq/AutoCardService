package com.chty.autocard.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.chty.autocard.config.AppConfig;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;

@Service
public class CheckInService {

    @Autowired
    private AppConfig appConfig;

    private Scheduler scheduler;

    public CheckInService() throws SchedulerException, IOException {
        this.appConfig = new AppConfig();
        SchedulerFactory factory = new StdSchedulerFactory();
        this.scheduler = factory.getScheduler();
        this.loadJobs();
    }

    private void loadJobs() throws SchedulerException {
        JSONArray jobs = appConfig.getJobs();
        for (Object job : jobs) {
            if (job instanceof JSONObject) {
                JSONObject jobJson = (JSONObject) job;
                JobDataMap jobDataMap = new JobDataMap(jobJson);
                String cron = jobJson.getString("cron");
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
        scheduler.start();
    }

}
