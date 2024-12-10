package ru.skillbox.auth_service.security.service;

import ru.skillbox.auth_service.web.dto.request.ChangeEmailRequest;
import ru.skillbox.auth_service.web.dto.request.ChangePasswordRequest;
import ru.skillbox.auth_service.web.dto.request.CreateUserRequest;
import ru.skillbox.auth_service.web.dto.request.LoginRequest;
import ru.skillbox.auth_service.web.dto.request.PasswordRecoveryRequest;
import ru.skillbox.auth_service.web.dto.request.RefreshTokenRequest;
import ru.skillbox.auth_service.web.dto.responce.AuthResponseDto;
import ru.skillbox.auth_service.web.dto.responce.CaptchaResponse;
import ru.skillbox.auth_service.web.dto.responce.RefreshTokenResponse;

public interface SecurityService {

    AuthResponseDto authenticationUser(LoginRequest loginRequest);

    void register(CreateUserRequest createUserRequest);

    RefreshTokenResponse refreshToken(RefreshTokenRequest request);

    void logout();

    Boolean validate(String token);

    CaptchaResponse generateCaptcha();

    byte[] sendCaptcha(String uuid);

    void passwordRecovery(PasswordRecoveryRequest request);

    String checkSecurityLink(String linkId);

    void changePassword(String email, ChangePasswordRequest changePasswordRequest);

    boolean changeEmail(String username, ChangeEmailRequest emailRequest);
}
