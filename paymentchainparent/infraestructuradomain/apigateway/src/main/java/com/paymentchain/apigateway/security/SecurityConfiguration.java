package com.paymentchain.apigateway.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfiguration {

    @Value("${SWAGGER_DEBUG_ALLOW_ALL:false}")
    private boolean swaggerDebugAllowAll;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http.csrf(ServerHttpSecurity.CsrfSpec::disable);

        if (swaggerDebugAllowAll) {
            http.authorizeExchange(exchange -> exchange
                    // 1. INFRAESTRUCTURA (Público)
                    .pathMatchers("/eureka/**", "/actuator/**").permitAll()

                    // Allow Swagger / OpenAPI endpoints and static resources (public for dev)
                    .pathMatchers(
                            "/swagger-ui.html",
                            "/swagger-ui/**",
                            "/v3/api-docs/**",
                            "/v3/api-docs",
                            "/v3/api-docs.yaml",
                            "/webjars/**",
                            "/swagger-resources/**",
                            "/favicon.ico"
                    ).permitAll()

                    // Open protected endpoints for testing
                    .pathMatchers(HttpMethod.GET, "/customers", "/accounts", "/transactions").permitAll()
                    .pathMatchers(HttpMethod.POST, "/transactions").permitAll()
                    .pathMatchers(HttpMethod.GET, "/customers/**", "/accounts/**", "/transactions/**").permitAll()

                    .anyExchange().authenticated()
            );
        } else {
            http.authorizeExchange(exchange -> exchange
                    // 1. INFRAESTRUCTURA (Público)
                    .pathMatchers("/eureka/**", "/actuator/**").permitAll()

                    // Allow Swagger / OpenAPI endpoints and static resources (public for dev)
                    .pathMatchers(
                            "/swagger-ui.html",
                            "/swagger-ui/**",
                            "/v3/api-docs/**",
                            "/v3/api-docs",
                            "/v3/api-docs.yaml",
                            "/webjars/**",
                            "/swagger-resources/**",
                            "/favicon.ico"
                    ).permitAll()

                    // Role-based rules
                    .pathMatchers(HttpMethod.GET, "/customers", "/accounts", "/transactions").hasRole("ADMIN")
                    .pathMatchers(HttpMethod.POST, "/transactions").hasAnyRole("ADMIN", "USER")
                    .pathMatchers(HttpMethod.GET, "/customers/**", "/accounts/**", "/transactions/**").hasAnyRole("ADMIN", "USER")

                    .anyExchange().authenticated()
            );
        }

        http.oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                        .jwtAuthenticationConverter(new KeycloakJwtAuthenticationConverter())
                )
        );

        return http.build();
    }

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        // Use allowed origin patterns so wildcard + credentials work in dev
        corsConfig.setAllowedOriginPatterns(List.of("*")); // for dev allow all; restrict in prod
        corsConfig.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        corsConfig.setAllowedHeaders(List.of("*"));
        corsConfig.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);
        return new CorsWebFilter(source);
    }
}
