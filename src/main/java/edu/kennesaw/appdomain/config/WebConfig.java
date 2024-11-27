package edu.kennesaw.appdomain.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("https://synergyaccounting.app", "https://www.synergyaccounting.app") // Allow both domains
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Allow common methods
                .allowedHeaders("*") // Allow all headers
                .exposedHeaders("X-CSRF-TOKEN") // Expose CSRF token for frontend
                .allowCredentials(true); // Enable cookies and credentials
    }
}

