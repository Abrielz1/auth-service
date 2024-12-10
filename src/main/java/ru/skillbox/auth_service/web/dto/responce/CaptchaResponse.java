package ru.skillbox.auth_service.web.dto.responce;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@Builder
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class CaptchaResponse {

    @Schema(description = "Captcha secret code/Секретный код капчи")
    private String secret;

    @Schema(description = "Captcha image/Картинка капчи")
    private String image;
}
