package ru.skillbox.auth_service.web.dto.responce;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDto {

    @Schema(description = "Access Token/Токе доступа")
    private String accessToken;

    @Schema(description = "Refresh Token/Токен")
    private String refreshToken;
}
