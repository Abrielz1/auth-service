package ru.skillbox.auth_service.security.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import ru.skillbox.auth_service.security.service.MyMailSender;
import ru.skillbox.auth_service.web.dto.request.PasswordRecoveryRequest;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class MyMailSenderImpl implements MyMailSender {

    private final JavaMailSender javaMailSender;

    private static final String TIME = " at time: ";

    @Value("${EMAIL_FROM}")
    private String from;

    @SneakyThrows
    @Transactional
    public void sendMailMessage(PasswordRecoveryRequest requestMail, StringBuilder recoveryLink) {

        log.info("Via MailSender sendMailMessage Request Mail: "
                + requestMail.getEmail()
                + " link: " + recoveryLink + TIME + LocalDateTime.now()
                + TIME + LocalDateTime.now() + LocalDateTime.now());

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(from);
        message.setTo(requestMail.getEmail());
        message.setSubject("Your password recovery link");
        message.setText("Your recovery link: " + System.lineSeparator() + recoveryLink.toString());

        javaMailSender.send(message);

        log.info("Email: %s sent successfully!".formatted(requestMail.getEmail())
                + TIME + LocalDateTime.now() + TIME + LocalDateTime.now() + LocalDateTime.now());
    }

    @SneakyThrows
    @Transactional
    public void sendMailMessage(String userEmail, String defaultPassword) {

        log.info("Via MailSender sendMailMessage Request Mail: "
                + userEmail + " default password: " + defaultPassword
                + TIME + LocalDateTime.now() + LocalDateTime.now());

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(from);
        message.setTo(userEmail);
        message.setSubject("Your default password");
        message.setText("Your default password: " + System.lineSeparator() + defaultPassword + " " +
                System.lineSeparator() + " Now you can login use default password : %s".formatted(defaultPassword)
                + TIME + LocalDateTime.now() + LocalDateTime.now());

        javaMailSender.send(message);

        log.info("Email: %s sent successfully!".formatted(userEmail)
                + TIME + LocalDateTime.now() + LocalDateTime.now());
    }
}
