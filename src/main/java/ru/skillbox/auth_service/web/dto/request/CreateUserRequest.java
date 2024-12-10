package ru.skillbox.auth_service.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class CreateUserRequest {

    @Schema(description = "Email user/Почта юзера")
    @NotNull
    private String uuid;

    @Schema(description = "флаг удалён/deleted ли пользователь")
    @NotNull
    private Boolean deleted;

    @Schema(description = "Email user/Почта юзера")
    @Email
    @NotBlank
    private String email;

    @Schema(description = "Password user1/Пароль юзера1")
    @Min(8)
    @Max(32)
    @NotBlank
    private String password1;

    @Schema(description = "Password user2/Пароль юзера2")
    @Min(8)
    @Max(32)
    @NotBlank
    private String password2;

    @Schema(description = "User first name/Имя пользователя")
    @Min(1)
    @Max(32)
    @NotBlank
    private String firstName;

    @Schema(description = "User last name/Фамилия пользователя")
    @Min(1)
    @Max(32)
    @NotBlank
    private String lastName;

    @Schema(description = "Captcha code/Код капчи")
    @NotBlank
    private String captchaCode;

    @Schema(description = "Captcha secret/Код ввод пользователя капчи")
    @NotBlank
    private String captchaSecret;
}
