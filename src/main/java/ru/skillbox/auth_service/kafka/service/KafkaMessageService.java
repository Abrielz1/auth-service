package ru.skillbox.auth_service.kafka.service;

import ru.skillbox.common.events.CommonEvent;
import ru.skillbox.common.events.UserEvent;

public interface KafkaMessageService {

   void updateUserEntity(CommonEvent<UserEvent> message);
}
