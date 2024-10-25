package ru.skillbox.auth_service.kafka.configuration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.skillbox.auth_service.exception.exceptions.ObjectNotFoundException;
import ru.skillbox.auth_service.kafka.dto.KafkaMessageInputDto;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaMessageServiceImpl implements KafkaMessageService {

    private final KafkaUserService kafkaUserService;

    @Override
    public void updateUserEntity(KafkaMessageInputDto message) {

        if (kafkaUserService.getUserFomDb(message.getUuid(), message.getEmail()).isPresent() &&
                kafkaUserService.checkUser(message.getUuid(), message.getEmail())) {

            log.info("User is not valid or not present in Db");
            throw new ObjectNotFoundException("User is not valid or not present in Db");
        }

        var isUser = kafkaUserService.getUserFomDb(message.getUuid(), message.getEmail()).get();

        kafkaUserService.saveUserToDb(kafkaUserService.updateUser(isUser));
    }
}
