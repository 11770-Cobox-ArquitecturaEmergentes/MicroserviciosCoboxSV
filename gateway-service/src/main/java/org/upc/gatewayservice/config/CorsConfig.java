package org.upc.gatewayservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();

        // Permitir orígenes (localhost con HTTP y HTTPS, más tu dominio y frontend)
        corsConfig.setAllowedOriginPatterns(Arrays.asList(
                "http://localhost",
                "http://localhost:*",
                "https://localhost",
                "https://localhost:*",
                "http://127.0.0.1",
                "http://127.0.0.1:*",
                "https://127.0.0.1",
                "https://127.0.0.1:*",
                "https://coboxsv.duckdns.org/",
                "https://frontend-cobox-sv.vercel.app"
        ));

        // Permitir todos los métodos HTTP
        corsConfig.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS", "HEAD"
        ));

        // Permitir todos los headers necesarios para Swagger y JWT
        corsConfig.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "Accept",
                "Origin",
                "X-Requested-With",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers"
        ));

        // Exponer headers en la respuesta
        corsConfig.setExposedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "Access-Control-Allow-Origin",
                "Access-Control-Allow-Credentials"
        ));

        // Permitir credenciales (cookies, headers de autorización)
        corsConfig.setAllowCredentials(true);

        // Tiempo de cache para la respuesta preflight (en segundos)
        corsConfig.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }
}

