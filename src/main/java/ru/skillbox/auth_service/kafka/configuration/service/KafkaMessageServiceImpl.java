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

      if (kafkaUserService.checkUser(message.getUuid(), message.getEmail())) {
          log.info("");
          throw new  ObjectNotFoundException("");
      }

        var isUser = kafkaUserService.getUserFomDb(message.getUuid(), message.getEmail()).get();

      if (Boolean.TRUE.equals(isUser.getDeleted())) {
          log.info("user is banned");
          kafkaUserService.updateUser(isUser);
          return;
      }

        kafkaUserService.saveUserToDb(kafkaUserService.updateUser(isUser));
    }
}
