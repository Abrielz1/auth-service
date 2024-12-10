package ru.skillbox.auth_service.security.service;

import ru.skillbox.auth_service.app.entity.RefreshToken;
import ru.skillbox.auth_service.app.entity.User;

import java.util.Optional;

public interface RefreshTokenService {

    Optional<RefreshToken> getByRefreshToken(String refreshToken);

    RefreshToken create(User user);

    RefreshToken checkRefreshToken(RefreshToken refreshToken);

    void deleteByUuid(String uuid);
}
