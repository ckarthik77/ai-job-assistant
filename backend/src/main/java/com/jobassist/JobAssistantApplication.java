package com.jobassist;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.beans.factory.annotation.Value;

/**
 * Main Spring Boot Application for AI Job Application Assistant
 *
 * This application provides AI-powered resume enhancement with ATS scoring.
 * It parses resumes, extracts keywords from job descriptions, and generates
 * optimized resumes in Google XYZ format.
 */
@SpringBootApplication
public class JobAssistantApplication {

    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    public static void main(String[] args) {
        SpringApplication.run(JobAssistantApplication.class, args);
    }

    /**
     * Configure CORS to allow frontend communication
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins(allowedOrigins.split(","))
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true)
                        .maxAge(3600);
            }
        };
    }
}
