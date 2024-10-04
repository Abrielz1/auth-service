package ru.skillbox.auth_service.web.dto;

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
public class RegistrationDto {

    @NotBlank
    private String uuid; // (UUID ?)

    @NotNull
    private Boolean deleted;

    @Email
    @NotBlank
    private String email;

    @Min(8)
    @Max(32)
    @NotBlank
    private String password;

    @Min(1)
    @Max(32)
    @NotBlank
    private String firstANme;

    @Min(1)
    @Max(32)
    @NotBlank
    private String lastName;

    @NotBlank
    private String captchaCod;

    @NotBlank
    private String captchaSecret;
}
