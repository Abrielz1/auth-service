package ru.skillbox.auth_service.web.mapper;

import ru.skillbox.auth_service.app.entity.User;
import ru.skillbox.auth_service.kafka.dto.KafkaMessageOutputDto;
import ru.skillbox.auth_service.web.dto.AuthResponseDto;

public class EntityDtoMapper {

    public static KafkaMessageOutputDto toDto(AuthResponseDto user) {

        new KafkaMessageOutputDto();
        return KafkaMessageOutputDto
                .builder()
                .email(user.getEmail())
                .password(user.getPassword())
                .token(user.getToken())
                .deleted(user.getDeleted())
                .build();
    }

    public static KafkaMessageOutputDto toDto(User user) {

        new KafkaMessageOutputDto();
        return KafkaMessageOutputDto
                .builder()
                .email(user.getEmail())
                .password(user.getPassword())
                .deleted(user.getDeleted())
                .build();
    }
}