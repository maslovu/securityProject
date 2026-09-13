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
                // Открываем доступ к метрикам без авторизации
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/actuator/**").permitAll()
                        // Защищаем все остальные эндпоинты
                        .anyExchange().authenticated())

                // Отключаем сессию, так как мы stateless
                .csrf(ServerHttpSecurity.CsrfSpec::disable)

                // Главная магия: говорим шлюзу, что он является OAuth2 Resource Server
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt ->
                                jwt.jwtAuthenticationConverter(new KeycloakReactiveJwtAuthenticationConverter())))
                .build();
    }
}
