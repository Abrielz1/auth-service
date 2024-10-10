package ru.skillbox.auth_service.kafka.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import java.util.List;

@Getter
@Setter
@Builder
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class KafkaMessageInputDto {

    private Long id;

    private String uuid;

    private String refreshToken;

    private Boolean isDeleted;

    private String email;

    private String password;

    private String password2;

    private String firstName;

    private String lastName;

    private List<String> roles;
}
