package com.movieDekho.MovieDekho;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan
@SpringBootApplication
public class MovieDekhoApplication {

	public static void main(String[] args) {
		SpringApplication.run(MovieDekhoApplication.class, args);
	}

}
