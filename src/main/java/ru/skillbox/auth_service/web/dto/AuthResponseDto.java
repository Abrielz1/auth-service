package ru.skillbox.auth_service.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDto {

    private Long id;

    private String token;

    private String refreshToken;

    private String password;

    private String password2;

    private String uuid;

    private Boolean isDeleted;

    private String firstName;

    private String lastName;

    private String email;

    private List<String> roles;
}
