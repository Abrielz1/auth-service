package ru.skillbox.auth_service.web.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.skillbox.auth_service.security.service.SecurityService;

/**
 * Здесь мы регистрируем пользователя и производим манипуляции с токеном и капчей
 */

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class UserAuthController {

    private final SecurityService service;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.OK)
    public String registerUser() {

        return "";
    }

    @PostMapping("/refresh")
    @ResponseStatus(HttpStatus.OK)
    public String refreshToken() {

        return "";
    }

    @PostMapping("/password/recovery")
    @ResponseStatus(HttpStatus.OK)
    public String passwordUserRecovery() {

        return "";
    }

    @PostMapping("/password/recovery/{linkId}")
    @ResponseStatus(HttpStatus.OK)
    public String passwordUserRecovery(@PathVariable(name = "linkId") Long linkId) {

        return "";
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public String authUser() {

        return "";
    }

    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    public String generateCaptcha() {

        return "";
    }
}
