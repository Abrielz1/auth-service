package ru.skillbox.auth_service.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisKeyExpiredEvent;
import org.springframework.stereotype.Component;
import ru.skillbox.auth_service.app.entity.RefreshToken;
import ru.skillbox.auth_service.exception.exceptions.RefreshTokenException;

@Slf4j
@Component
public class RedisExpirationEvent {

    @EventListener
    public void handelRedisKeyExpiredEvent(RedisKeyExpiredEvent<RefreshToken> event) {
        RefreshToken expiredRefreshToken = (RefreshToken) event.getValue();

        if (expiredRefreshToken == null) {
            throw new RefreshTokenException("Refresh token is null in handleRedisKeyExpiredEvent function");
        }

        log.info(("Refresh token" +
                " has expired and Refresh Token is %s").formatted(expiredRefreshToken.getToken()
                ));
    }
}
