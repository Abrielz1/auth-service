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

/**
 * Здесь мы регистрируем пользователя и производим манипуляции с токеном и капчей
 */

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class UserAuthController {


    //Todo: заинжектить сервис




    @PostMapping("/register")
    @ResponseStatus(HttpStatus.OK)
    public void registerUser() {

    }

    @PostMapping("/refresh")
    @ResponseStatus(HttpStatus.OK)
    public void refreshToken() {

    }

    @PostMapping("/password/recovery")
    @ResponseStatus(HttpStatus.OK)
    public void passwordUserRecovery() {

    }

    @PostMapping("/password/recovery/{linkId}")
    @ResponseStatus(HttpStatus.OK)
    public void passwordUserRecovery(@PathVariable(name = "linkId") Long linkId) {

    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public void authUser() {

    }

    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    public void generateCaptcha() {

    }
}
