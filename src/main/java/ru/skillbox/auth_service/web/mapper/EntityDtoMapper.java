package ru.skillbox.auth_service.web.mapper;

import ru.skillbox.auth_service.app.entity.User;
import ru.skillbox.auth_service.kafka.dto.KafkaMessageOutputDto;
import ru.skillbox.auth_service.web.dto.AuthResponseDto;

public class EntityDtoMapper {

    public static KafkaMessageOutputDto toDto(AuthResponseDto user) {

        new KafkaMessageOutputDto();
        return KafkaMessageOutputDto
                .builder()
                .id(user.getId())
                .uuid(user.getUuid())
                .isDeleted(user.getIsDeleted())
                .refreshToken(user.getToken())
                .refreshToken(user.getRefreshToken())
                .email(user.getEmail())
                .password(user.getPassword())
                .password2(user.getPassword2())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .roles(user.getRoles())
                .build();
    }

    public static KafkaMessageOutputDto toDto(User user) {

        new KafkaMessageOutputDto();
        return KafkaMessageOutputDto
                .builder()
                .id(user.getId())
                .uuid(user.getUuid())
                .isDeleted(user.getIsDeleted())
                .email(user.getEmail())
                .password(user.getPassword())
                .password2(user.getPassword2())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .roles(null)
                .build();
    }
}