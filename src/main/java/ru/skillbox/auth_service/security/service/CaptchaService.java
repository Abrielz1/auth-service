package ru.skillbox.auth_service.security.service;

public interface CaptchaService {

    byte[] generateCaptchaImage();

    String generateCaptcha();
}
