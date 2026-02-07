package com.rentease.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
public class AsyncConfig {
    // Enables async processing for email sending
    // Uses Spring's default SimpleAsyncTaskExecutor
    // For production, consider configuring a custom ThreadPoolTaskExecutor
}
