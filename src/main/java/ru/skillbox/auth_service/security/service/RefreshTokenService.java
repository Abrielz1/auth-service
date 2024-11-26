package ru.skillbox.auth_service.security.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.skillbox.auth_service.app.entity.RefreshToken;
import ru.skillbox.auth_service.app.entity.User;
import ru.skillbox.auth_service.app.repository.RefreshTokenRepository;
import ru.skillbox.auth_service.exception.exceptions.RefreshTokenException;
import ru.skillbox.auth_service.security.jwt.JwtUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    private final JwtUtils jwtUtils;

    @Value("${app.jwt.refreshTokenExpiration}")
    private Duration refreshTokenExpiration;

    public Optional<RefreshToken> getByRefreshToken(String refreshToken) {

        System.out.println("Refresh token in db: " + refreshTokenRepository.findByUuid(refreshToken).isPresent());

        return refreshTokenRepository.findByUuid(refreshToken);
    }

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