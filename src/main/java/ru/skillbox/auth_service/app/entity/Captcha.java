package ru.skillbox.auth_service.app.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

@Getter
@Setter
@Builder
@ToString
@RedisHash("captcha_image")
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class Captcha {

    @Id
    @Indexed
    private Long id;

    @Indexed
    private String uuid;

    @Indexed
    private byte[] image;
}
