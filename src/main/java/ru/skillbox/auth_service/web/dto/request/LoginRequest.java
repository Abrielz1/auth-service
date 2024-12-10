package ru.skillbox.auth_service.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * DTO аутентификации
 */

@Getter
@Setter
@Builder
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class
LoginRequest {

    @Schema(description = "Email user/Почта юзера")
    @Email
    @NotBlank
    private String email;

    @Schema(description = "Password user/Пароль юзера")
    @Min(8)
    @Max(32)
    @NotBlank
    private String password;
}
