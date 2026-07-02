package org.upc.gatewayservice.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.util.matcher.OrServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    @Order(1)
    public SecurityWebFilterChain publicRoutes(ServerHttpSecurity http) {
        http
                .securityMatcher(new OrServerWebExchangeMatcher(
                        new PathPatternParserServerWebExchangeMatcher("/api/v1/authentication/**"),
                        new PathPatternParserServerWebExchangeMatcher("/api/v1/jwks/**"),
                        new PathPatternParserServerWebExchangeMatcher("/swagger-ui.html"),
                        new PathPatternParserServerWebExchangeMatcher("/swagger-ui/**"),
                        new PathPatternParserServerWebExchangeMatcher("/webjars/**"),
                        new PathPatternParserServerWebExchangeMatcher("/v3/api-docs/**"),
                        new PathPatternParserServerWebExchangeMatcher("/*/v3/api-docs/**"),
                        new PathPatternParserServerWebExchangeMatcher("/swagger-resources/**"),
                        new PathPatternParserServerWebExchangeMatcher("/swagger-config/**"),
                        new PathPatternParserServerWebExchangeMatcher("/actuator/**")
                ))
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(Customizer.withDefaults())
                .authorizeExchange(exchange -> exchange
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .anyExchange().permitAll()
                );

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(Customizer.withDefaults())
                .authorizeExchange(exchange -> exchange
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(spec -> spec.jwt(Customizer.withDefaults()));

        return http.build();
    }
}