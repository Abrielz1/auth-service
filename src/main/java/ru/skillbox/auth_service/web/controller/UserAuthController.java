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
import java.util.ArrayList;
import java.util.List;

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
    public List<String> registerUser() {

        return new ArrayList<>();
    }

    @PostMapping("/refresh")
    @ResponseStatus(HttpStatus.OK)
    public List<String> refreshToken() {

        return new ArrayList<>();
    }

    @PostMapping("/password/recovery")
    @ResponseStatus(HttpStatus.OK)
    public List<String> passwordUserRecovery() {

        return new ArrayList<>();
    }

    @PostMapping("/password/recovery/{linkId}")
    @ResponseStatus(HttpStatus.OK)
    public List<String> passwordUserRecovery(@PathVariable(name = "linkId") Long linkId) {

        return new ArrayList<>();
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public List<String> authUser() {

        return new ArrayList<>();
    }

    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    public String generateCaptcha() {

        return "";
    }
}
