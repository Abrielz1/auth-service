package ru.skillbox.auth_service.app.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import java.time.Instant;

@Data
@Builder
@RedisHash("refresh_tokens")
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {

    /**
     * id рефреш токена
     */
    @Id
    @Indexed
    private Long id;

    /**
     * id пользователя
     */
    @Indexed
    private Long userId;

    /**
     * uuid пользователя
     */
    @Indexed
    private String uuid;

    /**
     * электронная пользователя
     */
    @Indexed
    private String email;

    /**
     * рефреш токен
     */
    @Indexed
    private String token;

    /**
     * время жизни токен
     */
    @Indexed
    private Instant expiryDate;
}
