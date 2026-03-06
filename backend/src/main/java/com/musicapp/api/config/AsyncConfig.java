package com.musicapp.api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Enables @Async processing for background tasks like HLS transcoding.
 */
@Configuration
@EnableAsync
public class AsyncConfig {
}
