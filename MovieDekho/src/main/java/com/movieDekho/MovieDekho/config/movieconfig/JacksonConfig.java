package com.movieDekho.MovieDekho.config.movieconfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.hibernate5.jakarta.Hibernate5JakartaModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        Hibernate5JakartaModule hibernate5Module = new Hibernate5JakartaModule();
        hibernate5Module.configure(Hibernate5JakartaModule.Feature.FORCE_LAZY_LOADING, false);
        mapper.registerModule(hibernate5Module);
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }
}
