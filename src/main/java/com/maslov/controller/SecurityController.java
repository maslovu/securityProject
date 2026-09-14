package com.maslov.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/hello")
public class SecurityController {

    @GetMapping
    public Mono<String> hello(Authentication auth) {
        // Здесь можно залогировать имя пользователя:
        //auth.getName() -> user's sub from Keycloak
        return Mono.just("Hello, " + auth.getName());
    }
}
