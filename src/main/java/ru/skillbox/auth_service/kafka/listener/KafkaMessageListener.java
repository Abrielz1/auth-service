package ru.skillbox.auth_service.kafka.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import ru.skillbox.auth_service.exception.exceptions.ObjectNotFoundException;
import ru.skillbox.auth_service.kafka.configuration.service.KafkaMessageService;
import ru.skillbox.common.events.CommonEvent;
import ru.skillbox.common.events.UserEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaMessageListener {

    private final KafkaMessageService kafkaMessageService;

    @KafkaListener(topics = "${app.kafka.kafkaMessageTopic0}",
            groupId = "${app.kafka.kafkaMessageGroupId0}",
            containerFactory = "kafkaMessageConcurrentKafkaListenerContainerFactory")
    public void receive(@Payload CommonEvent<UserEvent> message,
                        @Header(value = KafkaHeaders.RECEIVED_TOPIC) String topic) {

        log.info("Received message: {}", message.getData());
        log.info("Message: {}; Topic: {}, Time: {}", message, topic, System.currentTimeMillis());

        if (message.getData() == null) {
            throw new ObjectNotFoundException("No data to User update");
        }

        System.out.println("message: " + message.getData() + " time received in ms:" + System.currentTimeMillis());
        kafkaMessageService.updateUserEntity(message);
    }
}
