package com.movieDekho.MovieDekho.config.emailconfig;

import com.movieDekho.MovieDekho.service.emailService.EmailProcessorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailQueueShutdownConfig {

    private final EmailProcessorService emailProcessorService;

    @PreDestroy
    public void cleanup() {
        log.info("Shutting down email queue processor...");
        emailProcessorService.shutdown();
        log.info("Email queue processor shutdown completed");
    }
}