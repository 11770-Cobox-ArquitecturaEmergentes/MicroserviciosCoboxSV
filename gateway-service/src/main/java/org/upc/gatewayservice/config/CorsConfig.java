package org.upc.gatewayservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();

        corsConfig.setAllowedOriginPatterns(List.of(
                "http://localhost",
                "http://localhost:*",
                "https://localhost",
                "https://localhost:*",
                "http://127.0.0.1",
                "http://127.0.0.1:*",
                "https://127.0.0.1",
                "https://127.0.0.1:*",
                "https://coboxsv.duckdns.org",
                "https://frontend-cobox-sv.vercel.app",
                "https://*.vercel.app"
        ));

        corsConfig.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS", "HEAD"
        ));

        corsConfig.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "Accept",
                "Origin",
                "X-Requested-With",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers"
        ));

        corsConfig.setExposedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "X-RateLimit-Limit",
                "X-RateLimit-Remaining",
                "X-RateLimit-Reset"
        ));

        corsConfig.setAllowCredentials(true);
        corsConfig.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }
}