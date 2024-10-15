package ru.skillbox.auth_service.kafka.dto;

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
public class KafkaMessageOutputDto {

    private String uuid;

    private String email;

    private String password;

    private String token;

  //  private String refreshToken;

    private Boolean deleted;

}
