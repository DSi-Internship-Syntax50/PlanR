package com.example.PlanR;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PlanRApplication {

	public static void main(String[] args) {
		SpringApplication.run(PlanRApplication.class, args);
	}

}
