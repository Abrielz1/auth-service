package ru.skillbox.auth_service.web.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.skillbox.auth_service.security.service.SecurityService;
import ru.skillbox.auth_service.web.dto.AuthResponseDto;
import ru.skillbox.auth_service.web.dto.CaptchaRs;
import ru.skillbox.auth_service.web.dto.CreateUserRequest;
import ru.skillbox.auth_service.web.dto.LoginRequest;
import ru.skillbox.auth_service.web.dto.NewPasswordDto;
import ru.skillbox.auth_service.web.dto.PasswordRecoveryRequest;
import ru.skillbox.auth_service.web.dto.RefreshTokenRequest;
import ru.skillbox.auth_service.web.dto.RefreshTokenResponse;

/**
 * Здесь мы регистрируем пользователя и производим манипуляции с токеном и капчей
 */

@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class UserAuthController {

    private final SecurityService securityService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.OK)
    public String registerUser(@RequestBody CreateUserRequest request) {

        log.info("Via Auth Controller request is: %s".formatted(request));

        securityService.register(request);
        return "";
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public AuthResponseDto authUser(@RequestBody LoginRequest loginRequest) {

        log.info("Via Auth Controller login request is: %s".formatted(loginRequest));

        return securityService.authenticationUser(loginRequest);
    }

    @PostMapping("/refresh")
    @ResponseStatus(HttpStatus.OK)
    public RefreshTokenResponse refreshToken(@RequestBody RefreshTokenRequest request) {

        log.info("Via Auth Controller Refresh RefreshToken is: %s".formatted(request));
        return securityService.refreshToken(request);
    }

    @PostMapping("/password/recovery")
    @ResponseStatus(HttpStatus.OK)
    public void passwordUserRecovery(@RequestBody PasswordRecoveryRequest data) {

        log.info("Via Auth Controller Password recovery by email: %s".formatted(data.getEmail()));

        securityService.passwordRecovery(data);
    }

    @PostMapping("/password/recovery/{linkId}")
    @ResponseStatus(HttpStatus.OK)
    public String passwordUserRecovery(@PathVariable(name = "linkId") String linkId,
                                       @RequestBody NewPasswordDto request) {

        log.info("Via Auth Controller Password recovery %s".formatted(linkId) + "\n");

        return securityService.checkSecurityLink(linkId, request);
    }

    @GetMapping("/captcha")
    @ResponseStatus(HttpStatus.OK)
    public CaptchaRs generateCaptcha() {

        log.info("Via Auth Controller Captcha was generated");

        return securityService.generateCaptcha();
    }

    @GetMapping(value = "/captcha/displayImage/{secret}", produces = MediaType.IMAGE_PNG_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public byte[] sendCaptchaImage(@PathVariable(name = "secret") String secret) {

        log.info("Via Auth Controller Captcha was sent");

        return securityService.sendCaptcha(secret);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.OK)
    public String logout(@AuthenticationPrincipal UserDetails details) {

        securityService.logout();
        log.info("User was logout! Username is: %s".formatted(details.getUsername()));

        return "User was logout! Username is: " + details.getUsername();
    }

    @GetMapping("/validate")
    @ResponseStatus(HttpStatus.OK)
    public Boolean validate(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader) {

        log.info("Via Auth Controller validate(): ");
        log.info("This is key to validate: %s".formatted(authorizationHeader));

        return securityService.validate(authorizationHeader);
    }
}