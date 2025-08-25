package com.movieDekho.MovieDekho;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MovieDekhoApplication {

    public static void main(String[] args) {
        SpringApplication.run(MovieDekhoApplication.class, args);
    }
}
