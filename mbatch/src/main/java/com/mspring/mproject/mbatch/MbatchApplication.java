package com.mspring.mproject.mbatch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MbatchApplication {

	public static void main(String[] args) {
		SpringApplication.run(MbatchApplication.class, args);
	}

}
