package ru.skillbox.auth_service.security.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisKeyValueAdapter;
import org.springframework.data.redis.core.convert.KeyspaceConfiguration;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import ru.skillbox.auth_service.app.entity.Captcha;
import ru.skillbox.auth_service.app.entity.RefreshToken;

import java.time.Duration;
import java.util.List;

@Configuration
@EnableRedisRepositories(keyspaceConfiguration = RedisConfiguration.RefreshTokenKeyConfiguration.class,
        enableKeyspaceEvents = RedisKeyValueAdapter.EnableKeyspaceEvents.ON_STARTUP)
public class RedisConfiguration {

    @Value("${app.jwt.refreshTokenExpiration}")
    private Duration refreshTokenExpiration;

    @Value("${app.jwt.captchaLifeTime}")
    private Duration captchaExpiration;

    @Bean
    public JedisConnectionFactory jedisConnectionFactory(RedisProperties redisProperties) {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();

        configuration.setHostName(redisProperties.getHost());
        configuration.setPort(redisProperties.getPort());

        return new JedisConnectionFactory(configuration);
    }

    public class RefreshTokenKeyConfiguration extends KeyspaceConfiguration {

        private static final String REFRESH_TOKEN_KEYSPACE = "refresh_tokens";

        private static final String CAPTCHA_IMAGE_KEYSPACE = "captcha_image";

        @Override
        protected Iterable<KeyspaceSettings> initialConfiguration() {

            KeyspaceSettings keyspaceSettings = new KeyspaceSettings(RefreshToken.class, REFRESH_TOKEN_KEYSPACE);
            KeyspaceSettings captchaImageKeyspaceSettings = new KeyspaceSettings(Captcha.class, CAPTCHA_IMAGE_KEYSPACE);

            keyspaceSettings.setTimeToLive(refreshTokenExpiration.getSeconds());
            captchaImageKeyspaceSettings.setTimeToLive(captchaExpiration.getSeconds());

            return List.of(keyspaceSettings, captchaImageKeyspaceSettings);
        }
    }
}
