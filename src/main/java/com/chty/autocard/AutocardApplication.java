package com.chty.autocard;

import com.chty.autocard.service.CheckInService;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AutocardApplication {
	
	@Autowired
	CheckInService checkInService;

	public static void main(String[] args) {
		SpringApplication.run(AutocardApplication.class, args);
	}

	@Autowired
	public void registerCheckInService() throws SchedulerException {
		checkInService.start();
	}

}
