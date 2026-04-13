package com.movieDekho.MovieDekho.config.reqconfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration for REST client beans.
 */
@Configuration
public class RestClientConfig {

    /**
     * Create a RestTemplate bean for making HTTP requests to external APIs.
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
