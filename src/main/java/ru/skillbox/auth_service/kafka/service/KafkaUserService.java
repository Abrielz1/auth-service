package ru.skillbox.auth_service.kafka.service;

import ru.skillbox.auth_service.app.entity.User;

import java.util.Optional;

public interface KafkaUserService {

    boolean checkUser(String uuid, String email);

    Optional<User> getUserFomDb(String uuid, String email);

    void updateUser(User user);

    void disableUserAccount(String uuid, String email);

    void banUserAccount(String uuid, String email);
}
