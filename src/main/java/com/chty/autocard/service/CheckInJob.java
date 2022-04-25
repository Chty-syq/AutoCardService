package com.chty.autocard.service;

import com.chty.autocard.utils.AppUtils;
import lombok.SneakyThrows;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;

public class CheckInJob implements Job {

    private final CheckInOperation checkInOperation = AppUtils.getBean(CheckInOperation.class);

    @SneakyThrows
    @Override
    public void execute(JobExecutionContext context) {
        JobDataMap dataMap = context.getMergedJobDataMap();
        assert checkInOperation != null;
        checkInOperation.enact(dataMap);
    }
    
}
