package ru.skillbox.auth_service.security.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.skillbox.auth_service.app.entity.RefreshToken;
import ru.skillbox.auth_service.app.entity.User;
import ru.skillbox.auth_service.app.repository.RefreshTokenRepository;
import ru.skillbox.auth_service.exception.exceptions.RefreshTokenException;
import ru.skillbox.auth_service.security.jwt.JwtUtils;
import ru.skillbox.auth_service.security.service.RefreshTokenService;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

/**
 * Сервис управляет токенами
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    private final JwtUtils jwtUtils;

    @Value("${app.jwt.refreshTokenExpiration}")
    private Duration refreshTokenExpiration;

    /**
     * Получение сущности RefreshToken из Redis по RefreshToken(UUID)
     * @param refreshToken RefreshToken(UUID) по, которой происходит поиск в Redis
     * @return сущность RefreshToken
     */
    public Optional<RefreshToken> getByRefreshToken(String refreshToken) {

        System.out.println("Refresh token in db: " + refreshTokenRepository.findByUuid(refreshToken).isPresent());

        return refreshTokenRepository.findByUuid(refreshToken);
    }

    /**
     *
     * @param user
     * @return
     */
    public RefreshToken create(User user) {

        var refreshToken = RefreshToken
                .builder()
                .userId(user.getId())
                .uuid(user.getUuid())
                .email(user.getEmail())
                .expiryDate(Instant.now().plusMillis(refreshTokenExpiration.toMillis()))
                .token(jwtUtils.generateTokenFromUser(user))
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    /**
     *
     * @param refreshToken
     * @return
     */
    public RefreshToken checkRefreshToken(RefreshToken refreshToken) {

        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new RefreshTokenException("Refresh token is expired! " + refreshToken.getToken()
                    + "Try reLogin!");
        } else {

            return refreshToken;
        }
    }

    public void deleteByUuid(String uuid) {
        refreshTokenRepository.deleteByUuid(uuid);
    }
}