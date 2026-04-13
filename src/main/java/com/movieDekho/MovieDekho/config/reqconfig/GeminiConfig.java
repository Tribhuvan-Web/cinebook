package com.movieDekho.MovieDekho.config.reqconfig;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

/**
 * Configuration for Google Gemini AI integration.
 * Manages API credentials, model settings, and feature flags.
 */
@Configuration
@ConfigurationProperties(prefix = "gemini")
@Data
public class GeminiConfig {

    private Api api = new Api();
    private Model model = new Model();
    private Assistant assistant = new Assistant();

    @Data
    public static class Api {
        private String key;
        private String endpoint = "https://generativelanguage.googleapis.com/v1beta/models";
    }

    @Data
    public static class Model {
        private String name = "gemini-2.5-flash";
        private double temperature = 0.7; // 0.0-2.0
        private int maxTokens = 1000;
    }

    @Data
    public static class Assistant {
        private boolean enabled = true;
    }
}
