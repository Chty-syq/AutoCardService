package com.chty.autocard.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.chty.autocard.config.AppConfig;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;

@Component
public class CheckInService {

    @Autowired
    private AppConfig appConfig;

    @Autowired
    private CheckInOperation checkInOperation;

    private final Scheduler scheduler;
    
    @Value("${app.mode}")
    private Integer mode;

    @Value("${app.cron}")
    private String system_cron;

    public CheckInService() throws SchedulerException, IOException {
        SchedulerFactory factory = new StdSchedulerFactory();
        this.scheduler = factory.getScheduler();
    }

    private void loadJobs() throws SchedulerException, IOException, InterruptedException {
        JSONArray jobs = appConfig.getJobs();
        for (Object job : jobs) {
            if (job instanceof JSONObject) {
                JSONObject jobJson = (JSONObject) job;
                JobDataMap jobDataMap = new JobDataMap(jobJson);
                switch (mode) {
                    case 0:
                        this.addJob(CheckInJob.class, system_cron, new Date(), jobDataMap); break;
                    case 1:
                        checkInOperation.enact(jobDataMap); break;
                    case 2:
                        if(!jobDataMap.getBooleanValue("test")) break;
                        this.addJob(CheckInJob.class, "0/10 * * * * ? *", new Date(), jobDataMap); break;
                }
            }
        }
    }
    
    public void addJob(Class<? extends Job> jobClass, String cron, Date startTime, JobDataMap jobDataMap) throws SchedulerException {
        
        JobDetail jobDetail = JobBuilder.newJob(jobClass).usingJobData(jobDataMap).build();
        
        Trigger trigger = TriggerBuilder.newTrigger()
                .withSchedule(CronScheduleBuilder.cronSchedule(cron)).startAt(startTime).build();
        
        this.scheduler.scheduleJob(jobDetail, trigger);
    }
    
    public void start() throws SchedulerException, IOException, InterruptedException {
        this.loadJobs();
        scheduler.start();
    }

}
