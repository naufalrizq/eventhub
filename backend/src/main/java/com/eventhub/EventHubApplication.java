package com.eventhub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * EventHub Application - Event Management Platform
 * 
 * A comprehensive event management system built with Spring Boot that provides:
 * - Event creation and management
 * - User registration and ticketing
 * - Payment processing
 * - Analytics and reporting
 * - Email notifications
 * - QR code generation for tickets
 * 
 * @author EventHub Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableCaching
@EnableAsync
@EnableScheduling
public class EventHubApplication {

    public static void main(String[] args) {
        SpringApplication.run(EventHubApplication.class, args);
    }
}