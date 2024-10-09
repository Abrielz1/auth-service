package ru.skillbox.auth_service.web.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.skillbox.auth_service.app.repository.UserRepository;
import ru.skillbox.auth_service.exception.exceptions.AlreadyExistsException;
import ru.skillbox.auth_service.security.service.SecurityService;
import ru.skillbox.auth_service.web.dto.AuthResponseDto;
import ru.skillbox.auth_service.web.dto.CreateUserRequest;
import ru.skillbox.auth_service.web.dto.LoginRequest;
import ru.skillbox.auth_service.web.dto.RefreshTokenRequest;
import ru.skillbox.auth_service.web.dto.RefreshTokenResponse;

/**
 * Здесь мы регистрируем пользователя и производим манипуляции с токеном и капчей
 */

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class UserAuthController {

    private final SecurityService securityService;

    private final UserRepository userRepository;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.OK)
    public String registerUser(@RequestBody CreateUserRequest request) {

        if (userRepository.existsByEmail(request.getEmail()) &&
                userRepository.existsByUuid(request.getUuid())) {

            throw new AlreadyExistsException("User with" +
                    " entered email: %s and uuid: %s already exists!"
                            .formatted(request.getEmail(), request.getUuid()));
        }

        securityService.register(request);
        return "User was created!";
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public AuthResponseDto authUser(@RequestBody LoginRequest loginRequest) {

        return securityService.authenticationUser(loginRequest);
    }

    @PostMapping("/refresh")
    @ResponseStatus(HttpStatus.OK)
    public RefreshTokenResponse refreshToken(@RequestBody RefreshTokenRequest request) {

        return securityService.refreshToken(request);
    }

    @PostMapping("/password/recovery")
    @ResponseStatus(HttpStatus.OK)
    public String passwordUserRecovery() {

        // TODO
        return "";
    }

    @PostMapping("/password/recovery/{linkId}")
    @ResponseStatus(HttpStatus.OK)
    public String passwordUserRecovery(@PathVariable(name = "linkId") Long linkId) {

        // TODO
        return "";
    }

    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    public String generateCaptcha(@AuthenticationPrincipal UserDetails details) {
        securityService.logout();

        return "User was logout! Username is: " + details.getUsername();
    }
}
