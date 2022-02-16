package com.chty.autocard;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;

@SpringBootTest
class AutocardApplicationTests {

	@Autowired
	private JavaMailSender javaMailSender;
	
	@Test
	void contextLoads() {
		Assertions.assertNotNull(javaMailSender);
	}

}
