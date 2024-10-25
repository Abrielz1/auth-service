package ru.skillbox.auth_service.security.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.skillbox.auth_service.app.entity.RefreshToken;
import ru.skillbox.auth_service.app.entity.User;
import ru.skillbox.auth_service.app.repository.RefreshTokenRepository;
import ru.skillbox.auth_service.app.repository.UserRepository;
import ru.skillbox.auth_service.exception.exceptions.ObjectNotFoundException;
import ru.skillbox.auth_service.exception.exceptions.RefreshTokenException;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    @Value("${app.jwt.refreshTokenExpiration}")
    private Duration refreshTokenExpiration;

    private final RefreshTokenRepository refreshTokenRepository;

    private final UserRepository userRepository;

    public Optional<RefreshToken> getByRefreshToken(String refreshToken) {

        return refreshTokenRepository.findByToken(refreshToken);
    }

    public RefreshToken create(Long userId) {

        User user = userRepository.findById(userId).orElseThrow(()->
                new ObjectNotFoundException("Your userId: %d not present in Db"
                        .formatted(userId)));

        var refreshToken = RefreshToken
                .builder()
                .userId(user.getId())
                .email(user.getEmail())
                .uuid(user.getUuid())
                .expiryDate(Instant.now().plusMillis(refreshTokenExpiration.toMillis()))
                .token(UUID.randomUUID().toString())
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

    public Boolean checkRefreshToken(String refreshToken) {

        var tokenToDelete = this.refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(()->
                        new ObjectNotFoundException("Your token: %s not present in Db"
                                .formatted(refreshToken)));

        if (tokenToDelete.getExpiryDate().isBefore((Instant.now()))) {

           refreshTokenRepository.delete(tokenToDelete);
            throw new RefreshTokenException("Refresh token is expired! " + refreshToken
                    + "Try reLogin!");
        } else {

            return true;
        }
    }

    public void deleteByUuid(String uuid) {
        refreshTokenRepository.deleteByUuid(uuid);
    }
}
