package com.chty.autocard;

import com.chty.autocard.service.CheckInService;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.io.IOException;

@SpringBootApplication
public class AutocardApplication {
	
	@Autowired
	CheckInService checkInService;
	
	public static ApplicationContext applicationContext;

	public static void main(String[] args) {
		applicationContext = SpringApplication.run(AutocardApplication.class, args);
	}

	@Autowired
	public void registerCheckInService() throws SchedulerException, IOException, InterruptedException {
		checkInService.start();
	}

}
