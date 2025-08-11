package com.movieDekho.MovieDekho.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${movieDekho.openapi.dev-url:http://localhost:8080}")
    private String devUrl;

    @Value("${movieDekho.openapi.prod-url:https://your-production-url.com}")
    private String prodUrl;

    @Bean
    public OpenAPI movieDekhoOpenAPI() {
        Server devServer = new Server();
        devServer.setUrl(devUrl);
        devServer.setDescription("Server URL in Development environment");

        Server prodServer = new Server();
        prodServer.setUrl(prodUrl);
        prodServer.setDescription("Server URL in Production environment");

        Contact contact = new Contact();
        contact.setEmail("tribhuvannath4567@gmail.com");
        contact.setName("CineBook");
        contact.setUrl(prodUrl);


        Info info = new Info()
                .title("CineBook API")
                .version("1.0")
                .contact(contact)
                .description("This API provides comprehensive movie ticket booking functionality including user management, movie listings, seat reservations, and administrative controls.");

        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .name("JWT Authentication")
                .description("Enter JWT Bearer token");

        SecurityRequirement securityRequirement = new SecurityRequirement().addList("JWT Authentication");

        return new OpenAPI()
                .info(info)
                .servers(List.of(devServer, prodServer))
                .addSecurityItem(securityRequirement)
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("JWT Authentication", securityScheme));
    }
}
