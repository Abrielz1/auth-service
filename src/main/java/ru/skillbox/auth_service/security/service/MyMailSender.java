package ru.skillbox.auth_service.security.service;

import ru.skillbox.auth_service.web.dto.request.PasswordRecoveryRequest;

public interface MyMailSender {

    void sendMailMessage(PasswordRecoveryRequest requestMail, StringBuilder recoveryLink);

    void sendMailMessage(String userEmail, String defaultPassword);
}
