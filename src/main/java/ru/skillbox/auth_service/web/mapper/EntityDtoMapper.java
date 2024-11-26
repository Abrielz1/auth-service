package ru.skillbox.auth_service.web.mapper;

import ru.skillbox.auth_service.app.entity.User;
import ru.skillbox.common.events.CommonEvent;
import ru.skillbox.common.events.RegUserEvent;
import ru.skillbox.common.events.UserEvent;

import java.util.UUID;

public class EntityDtoMapper {

    private EntityDtoMapper() {
        throw new IllegalArgumentException("Utility clas!");
    }

    public static User toDto(CommonEvent<UserEvent> commonEvent ) {

        UserEvent userEvent = commonEvent.getData();

        new User();
        return User
                .builder()
                .uuid(userEvent.getId().toString())
                .email(userEvent.getEmail())
                .password1(userEvent.getPassword())
                .password2(userEvent.getPassword())
                .deleted(userEvent.isDeleted())
                .blocked(userEvent.isBlocked())
                .build();
    }

    public static CommonEvent<RegUserEvent> toEvent(User user) {

        RegUserEvent regUserEvent = new RegUserEvent();

        regUserEvent.setId(UUID.fromString(user.getUuid()));
        regUserEvent.setFirstName(user.getFirstName());
        regUserEvent.setLastName(user.getLastName());
        regUserEvent.setEmail(user.getEmail());
        regUserEvent.setPassword(user.getPassword1());

        return new CommonEvent<>("RegUserEvent", regUserEvent);
    }
}
