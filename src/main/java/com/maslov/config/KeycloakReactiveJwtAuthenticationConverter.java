package com.maslov.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import reactor.core.publisher.Mono;
import reactor.util.annotation.NonNull;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class KeycloakReactiveJwtAuthenticationConverter implements Converter<Jwt, Mono<AbstractAuthenticationToken>> {

    // Используем блокирующий конвертер напрямую, так как чтение полей из JWT — это синхронная операция в памяти
    private final JwtGrantedAuthoritiesConverter defaultAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();

    @Override
    public Mono<AbstractAuthenticationToken> convert(@NonNull Jwt jwt) {
        // 1. Создаем изменяемый Set для всех прав
        Set<GrantedAuthority> allAuthorities = new HashSet<>();

        // 2. Извлекаем стандартные scope (например, SCOPE_read), метод гарантированно возвращает Collection<GrantedAuthority>
        allAuthorities.addAll(defaultAuthoritiesConverter.convert(jwt));


        // 3. Добавляем роли из Keycloak
        allAuthorities.addAll(extractKeycloakRoles(jwt));

        // 4. Оборачиваем готовый токен в реактивный Mono.just
        String principalClaimName = jwt.getClaimAsString("preferred_username");
        return Mono.just(new JwtAuthenticationToken(jwt, allAuthorities, principalClaimName));
    }

    @SuppressWarnings("unchecked")
    private Collection<GrantedAuthority> extractKeycloakRoles(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess == null || !realmAccess.containsKey("roles")) {
            return Collections.emptyList();
        }

        List<String> roles = (List<String>) realmAccess.get("roles");
        return roles.stream()
                .map(roleName -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + roleName)) // Spring требует префикс ROLE_
                .collect(Collectors.toList());
    }
}
