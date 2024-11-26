package ru.skillbox.auth_service.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import ru.skillbox.auth_service.app.entity.model.RoleType;

import java.util.Date;
import java.util.Set;

@Setter
@Getter
@Builder
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class ValidateUserDetails {

    private String uuid;

    private String email;

    private Set<RoleType> roles;

    private Date iat;

    private Date exp;
}
