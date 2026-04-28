package com.keyloop.interview;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Unified Service Scheduler — monolithic deployment (JWT RS256 + RBAC, JPA/Flyway, Redis lock/blacklist hooks).
 */
@SpringBootApplication
public class InterviewApplication {

	public static void main(String[] args) {
		SpringApplication.run(InterviewApplication.class, args);
	}
}
