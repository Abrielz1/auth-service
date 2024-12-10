package ru.skillbox.auth_service.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import ru.skillbox.auth_service.web.dto.request.ChangeEmailRequest;
import ru.skillbox.auth_service.web.dto.request.ChangePasswordRequest;
import ru.skillbox.auth_service.web.dto.request.CreateUserRequest;
import ru.skillbox.auth_service.web.dto.request.LoginRequest;
import ru.skillbox.auth_service.web.dto.request.PasswordRecoveryRequest;
import ru.skillbox.auth_service.web.dto.request.RefreshTokenRequest;
import ru.skillbox.auth_service.web.dto.responce.AuthResponseDto;
import ru.skillbox.auth_service.web.dto.responce.CaptchaResponse;
import ru.skillbox.auth_service.web.dto.responce.RefreshTokenResponse;

import java.time.LocalDateTime;

/**
 * Здесь мы регистрируем пользователя и производим манипуляции с токеном и капчёй
 */
@Tag(name = "AuthenticationController", description = "Контроллер регистрации/авторизации")
@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class UserAuthController {

    private final SecurityService securityService;

    private static final String TIME = " at time: ";

    @Operation(
            summary = "Регистрация пользователя",
            description = "Позволяет зарегистрировать пользователя"
    )
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.OK)
    public String registerUser(@RequestBody CreateUserRequest request) {

        log.info("%nVia Auth Controller request is: %s".formatted(request)
                + TIME + LocalDateTime.now()+ System.lineSeparator());

        securityService.register(request);
        return "";
    }

    @Operation(
            summary = "Авторизация/Логин/Вход в учётную запись пользователя",
            description = "Позволяет авторизовать пользователя"
    )
    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public AuthResponseDto authUser(@RequestBody LoginRequest loginRequest) {

        log.info("Via Auth Controller login request is: %s".formatted(loginRequest)
                + TIME + LocalDateTime.now()+ System.lineSeparator());

        return securityService.authenticationUser(loginRequest);
    }

    @Operation(
            summary = "Обновление токена авторизации пользователя",
            description = "Позволяет обновить сессию без перелогинивания пользователя"
    )
    @PostMapping("/refresh")
    @ResponseStatus(HttpStatus.OK)
    public RefreshTokenResponse refreshToken(@RequestBody RefreshTokenRequest request) {

        log.info("%nVia Auth Controller Refresh RefreshToken is: %s".formatted(request)
                + TIME + LocalDateTime.now()+ System.lineSeparator());
        return securityService.refreshToken(request);
    }

    @Operation(
            summary = "Сброс пароля путём генерации ссылки для пользователя",
            description = "Позволяет сгенерировать ссылку для сброса пароля и отправить его на почту пользователя пользователя"
    )
    @PostMapping("/password/recovery")
    @ResponseStatus(HttpStatus.OK)
    public void passwordUserRecovery(@RequestBody PasswordRecoveryRequest data) {

        log.info("%nVia Auth Controller Password recovery by email: %s".formatted(data.getEmail())
                + TIME + LocalDateTime.now()+ System.lineSeparator());

        securityService.passwordRecovery(data);
    }

    @Operation(
            summary = "Путём перехода по сгенерированной ссылке происходит сброс пароля на пароль по умолчанию",
            description = "Позволяет установить пароль по умолчанию и отправить его на почту пользователя"
    )
    @GetMapping("/password/recovery/{linkId}")
    @ResponseStatus(HttpStatus.OK)
    public String passwordUserRecovery(@PathVariable(name = "linkId") String linkId) {

        log.info("%nVia Auth Controller Password recovery %s".formatted(linkId)
                + TIME + LocalDateTime.now()
        + System.lineSeparator());

        return securityService.checkSecurityLink(linkId);
    }

    @Operation(
            summary = "Генерирует капчу",
            description = "Генерирует капчу и кладёт в редис по UUID"
    )
    @GetMapping("/captcha")
    @ResponseStatus(HttpStatus.OK)
    public CaptchaResponse generateCaptcha() {

        log.info("%nVia Auth Controller Captcha was generated"
                + TIME + LocalDateTime.now()
                + System.lineSeparator());

        return securityService.generateCaptcha();
    }

    @Operation(
            summary = "Высылает капчу",
            description = "Высылает капчу и достаёт из редис по UUID"
    )
    @GetMapping(value = "/captcha/displayImage/{secret}", produces = MediaType.IMAGE_PNG_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public byte[] sendCaptchaImage(@PathVariable(name = "secret") String secret) {

        log.info("%nVia Auth Controller Captcha was sent"
                + TIME + LocalDateTime.now()
                + System.lineSeparator());

        return securityService.sendCaptcha(secret);
    }

    @Operation(
            summary = "ДеАвторизация/ЛогАут/Выход из учётной записи пользователя",
            description = "Позволяет окончить сессию пользователя"
    )
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.OK)
    public String logout(@AuthenticationPrincipal UserDetails details) {

        securityService.logout();
        log.info("%nUser was logout! Username is: %s".formatted(details.getUsername())
                + TIME + LocalDateTime.now()
                + System.lineSeparator());

        return "%nUser was logout! Username is: " + details.getUsername();
    }

    @Operation(
            summary = "Проверяет токен по подписи",
            description = "соберает токен по по пэйлоаду и собирает токен, затем сравнивает подписи"
    )
    @GetMapping("/validate")
    @ResponseStatus(HttpStatus.OK)
    public Boolean validate(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader) {

        log.info(System.lineSeparator() + "Via Auth Controller validate(): ");
        log.info(System.lineSeparator() + "This is key to validate: %s".formatted(authorizationHeader)
                + TIME + LocalDateTime.now()
                + System.lineSeparator());

        return securityService.validate(authorizationHeader);
    }

    @PostMapping("/change-password-link")
    public String changePassword(@RequestBody ChangePasswordRequest changePasswordRequest,
                                 @AuthenticationPrincipal UserDetails userDetails) {

        System.out.println("change-password-link: " + changePasswordRequest);
        securityService.changePassword(userDetails.getUsername(), changePasswordRequest);
        return "Ok";
    }

    @PostMapping("/change-email-link")
    public boolean email(@RequestBody ChangeEmailRequest emailRequest,
                         @AuthenticationPrincipal UserDetails userDetails) {

        System.out.println("change-email-link: " + emailRequest);
        return securityService.changeEmail(userDetails.getUsername(), emailRequest);
    }
}