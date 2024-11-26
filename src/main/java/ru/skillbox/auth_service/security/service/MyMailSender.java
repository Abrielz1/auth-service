package ru.skillbox.auth_service.security.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import ru.skillbox.auth_service.web.dto.PasswordRecoveryRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class MyMailSender {

    private final JavaMailSender javaMailSender;

    @Value("${EMAIL_FROM}")
    private String from;

    @SneakyThrows
    @Transactional
    public void sendMailMessage(PasswordRecoveryRequest requestMail, String recoveryLink) {

        log.info("Via MailSender sendMailMessage Request Mail: "
                + requestMail.getEmail() + " link: " + recoveryLink);

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(from);
        message.setTo(requestMail.getEmail());
        message.setSubject("Your password recovery link");
        message.setText("Your recovery link: " + System.lineSeparator() + recoveryLink);

        javaMailSender.send(message);

        log.info("Email: %s sent successfully!".formatted(requestMail.getEmail()));
    }
}
