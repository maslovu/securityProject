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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class KeycloakReactiveJwtAuthenticationConverter implements Converter<Jwt, Mono<AbstractAuthenticationToken>> {

    // Synchronous converter for standard scopes (SCOPE_xxx)
    private final JwtGrantedAuthoritiesConverter defaultAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();

    @Override
    public Mono<AbstractAuthenticationToken> convert(@NonNull Jwt jwt) {
        // 1. Create a mutable Set for all rights
        Set<GrantedAuthority> allAuthorities = new HashSet<>();

        // 2. Extracts standard scopes (e.g., SCOPE_read); the method is guaranteed to return a Collection<GrantedAuthority>
        allAuthorities.addAll(defaultAuthoritiesConverter.convert(jwt));

        // 3. Adding roles from Keycloak
        allAuthorities.addAll(extractKeycloakRoles(jwt));

        // 4. Wrap the resulting token in a reactive `Mono.just`
        String principalClaimName = jwt.getClaimAsString("preferred_username");
        if (principalClaimName == null) {
            principalClaimName = jwt.getSubject();
        }
        return Mono.just(new JwtAuthenticationToken(jwt, allAuthorities, principalClaimName));
    }

    @SuppressWarnings("unchecked")
    private Collection<GrantedAuthority> extractKeycloakRoles(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
        if (realmAccess == null || !realmAccess.containsKey("roles")) {
            return List.of();
        }

        List<String> roles = (List<String>) realmAccess.get("roles");
        return roles.stream()
                .map(roleName -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + roleName)) // Spring требует префикс ROLE_
                .toList();
    }
}
