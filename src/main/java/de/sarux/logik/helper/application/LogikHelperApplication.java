package de.sarux.logik.helper.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
public class LogikHelperApplication {

    public static void main(String[] args) {
        SpringApplication.run(LogikHelperApplication.class, args);
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/solve").allowedOrigins("http://localhost:4200");
                registry.addMapping("/detektor").allowedOrigins("http://localhost:4200");
                registry.addMapping("/positioner").allowedOrigins("http://localhost:4200");
            }
        };
    }
}