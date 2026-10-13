package com.maslov.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                // Permit all access to metrics
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/actuator/**").permitAll()
                        // Secure all other endpoints
                        .anyExchange().authenticated())

                // Disable CSRF — stateless API
                .csrf(ServerHttpSecurity.CsrfSpec::disable)

                // Configure as OAuth2 Resource Server
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt ->
                                jwt.jwtAuthenticationConverter(new KeycloakReactiveJwtAuthenticationConverter())))
                .build();
    }
}
