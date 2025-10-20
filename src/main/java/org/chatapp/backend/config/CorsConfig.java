package org.chatapp.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                // Local development frontends (common ports)
                .allowedOrigins(
                        "http://localhost:4200",
                        "http://127.0.0.1:4200",
                        "http://localhost:8080",
                        "http://127.0.0.1:8080",
                        "http://localhost:8081",
                        "http://127.0.0.1:8081",
                        "http://localhost:8082",
                        "http://127.0.0.1:8082",
                        "http://localhost",
                        "http://127.0.0.1"
                )
                // Allow production domain(s)
                .allowedOriginPatterns(
                        "https://chatapp.moses.it.com",
                        "http://chatapp.moses.it.com",
                        "https://*.moses.it.com"
                )
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("Authorization", "Content-Type")
                .allowCredentials(true);
    }

    // Ensure Spring Security's CORS filter uses the same configuration
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOrigins(Arrays.asList(
                "http://localhost:4200",
                "http://127.0.0.1:4200",
                "http://localhost:8080",
                "http://127.0.0.1:8080",
                "http://localhost:8081",
                "http://127.0.0.1:8081",
                "http://localhost:8082",
                "http://127.0.0.1:8082",
                "http://localhost",
                "http://127.0.0.1"
        ));
        config.setAllowedOriginPatterns(Arrays.asList(
                "https://chatapp.moses.it.com",
                "http://chatapp.moses.it.com",
                "https://*.moses.it.com"
        ));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("*"));
        config.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
