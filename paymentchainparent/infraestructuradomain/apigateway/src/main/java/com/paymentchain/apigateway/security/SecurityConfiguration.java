package com.paymentchain.apigateway.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfiguration {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchange -> exchange

                        // 1. INFRAESTRUCTURA (Público)
                        // Eureka y Actuator siempre abiertos para que el sistema no falle
                        .pathMatchers("/eureka/**", "/actuator/**").permitAll()

                        // 2. REGLAS DE "SOLO ADMIN" (Listados Completos)
                        // Si alguien quiere ver TODA la lista, debe ser ADMIN
                        .pathMatchers(HttpMethod.GET, "/customers", "/accounts", "/transactions").hasRole("ADMIN")

                        // 3. OPERACIONES DE CLIENTE (Y Admin también)
                        // Hacer transferencias (POST) -> Clientes y Admins
                        .pathMatchers(HttpMethod.POST, "/transactions").hasAnyRole("ADMIN", "USER")

                        // Consultar cosas específicas (por ID) -> Clientes y Admins
                        // Esto cubre: ver mi perfil, ver mi cuenta, ver un recibo
                        .pathMatchers(HttpMethod.GET, "/customers/**", "/accounts/**", "/transactions/**").hasAnyRole("ADMIN", "USER")

                        // 4. RESTO BLOQUEADO (Seguridad por defecto)
                        // Cualquier otra cosa (borrar, editar configuración, etc.) requiere autenticación
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(new KeycloakJwtAuthenticationConverter())
                        )
                );

        return http.build();
    }
}
