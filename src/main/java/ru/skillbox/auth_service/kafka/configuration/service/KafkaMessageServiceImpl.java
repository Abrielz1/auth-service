package ru.skillbox.auth_service.kafka.configuration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.skillbox.auth_service.app.entity.User;
import ru.skillbox.auth_service.exception.exceptions.ObjectNotFoundException;
import ru.skillbox.auth_service.web.mapper.EntityDtoMapper;
import ru.skillbox.common.events.CommonEvent;
import ru.skillbox.common.events.UserEvent;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaMessageServiceImpl implements KafkaMessageService {

    private final KafkaUserService kafkaUserService;

    @Override
    public void updateUserEntity(CommonEvent<UserEvent> message) {

        User userAccountToUpdate = EntityDtoMapper.toDto(message);

        if (kafkaUserService.getUserFomDb(message.getData().getId().toString(), message.getData().getEmail()).isPresent() &&
              kafkaUserService.checkUser(message.getData().getId().toString(), message.getData().getEmail())) {

            log.info("User is not valid or not present in Db");
            throw new ObjectNotFoundException("User is not valid or not present in Db");
        }

        kafkaUserService.saveUserToDb(kafkaUserService.updateUser(userAccountToUpdate));
    }
}
