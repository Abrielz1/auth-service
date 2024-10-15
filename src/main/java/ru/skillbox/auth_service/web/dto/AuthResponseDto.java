package ru.skillbox.auth_service.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.skillbox.auth_service.app.entity.RefreshToken;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDto {

    private String uuid;

    private String token;

    private RefreshToken refreshToken;

    private String email;
}
