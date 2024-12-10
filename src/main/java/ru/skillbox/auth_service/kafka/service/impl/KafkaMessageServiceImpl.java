package ru.skillbox.auth_service.kafka.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.skillbox.auth_service.app.entity.User;
import ru.skillbox.auth_service.exception.exceptions.ObjectNotFoundException;
import ru.skillbox.auth_service.kafka.service.KafkaMessageService;
import ru.skillbox.auth_service.kafka.service.KafkaUserService;
import ru.skillbox.auth_service.web.mapper.EntityDtoMapper;
import ru.skillbox.common.events.CommonEvent;
import ru.skillbox.common.events.UserEvent;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class KafkaMessageServiceImpl implements KafkaMessageService {

    private final KafkaUserService kafkaUserService;

    @Override
    @Transactional
    public void updateUserEntity(CommonEvent<UserEvent> message) {

        User userAccountToUpdate = EntityDtoMapper.toDto(message);

        if (kafkaUserService.getUserFomDb(message.getData().getId().toString(), message.getData().getEmail()).isEmpty() &&
                kafkaUserService.checkUser(message.getData().getId().toString(), message.getData().getEmail())) {

            log.info("%nUser is not valid or not present in Db" + System.lineSeparator());
            throw new ObjectNotFoundException("%nUser is not valid or not present in Db" + System.lineSeparator());
        }

        log.info(("%nUser was send to update via" +
                " KafkaMessageServiceImpl -> updateUserEntity: %s").formatted(userAccountToUpdate)
                + System.lineSeparator());
        kafkaUserService.updateUser(userAccountToUpdate);
    }
}
