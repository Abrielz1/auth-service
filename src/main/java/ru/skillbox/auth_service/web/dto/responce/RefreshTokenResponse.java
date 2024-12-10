package ru.skillbox.auth_service.web.dto.responce;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenResponse {

   @Schema(description = "Access Token/Токе доступа")
   private String accessToken;

   @Schema(description = "Refresh Token/Токен")
   private String refreshToken;
}
