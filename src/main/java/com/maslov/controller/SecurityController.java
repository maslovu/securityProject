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
        // auth.getName() returns preferred_username from JWT, not user sub
        return Mono.just("Hello, " + auth.getName());
    }
}
