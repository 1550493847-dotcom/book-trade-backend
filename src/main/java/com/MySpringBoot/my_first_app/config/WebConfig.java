package com.MySpringBoot.my_first_app.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String origins = System.getenv().getOrDefault("CORS_ORIGINS",
                "http://localhost:5173,http://localhost:8080,https://1550493847-dotcom.github.io");
        String[] originList = origins.split(",");
        registry.addMapping("/**")
                .allowedOriginPatterns(originList)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
