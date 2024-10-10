package ru.skillbox.auth_service.kafka.configuration.service;

import ru.skillbox.auth_service.kafka.dto.KafkaMessageInputDto;

public interface KafkaMessageService {

   void updateUserEntity(KafkaMessageInputDto message);
}
