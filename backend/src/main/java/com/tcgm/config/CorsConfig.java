package com.tcgm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        
        // Autoriser les origines du frontend
        config.setAllowedOrigins(Arrays.asList(
            "http://localhost:5173",   // Vite par défaut
            "http://localhost:3000",   // React par défaut
            "http://localhost:4200"    // Angular si utilisé
        ));
        
        // Autoriser les méthodes HTTP
        config.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));
        
        // Autoriser tous les headers
        config.setAllowedHeaders(Arrays.asList("*"));
        
        // Autoriser les credentials (cookies, autorisations)
        config.setAllowCredentials(true);
        
        // Durée de vie de la réponse CORS (en secondes)
        config.setMaxAge(3600L);
        
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}