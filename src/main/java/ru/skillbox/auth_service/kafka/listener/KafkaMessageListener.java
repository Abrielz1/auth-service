package ru.skillbox.auth_service.kafka.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import ru.skillbox.auth_service.kafka.configuration.service.KafkaMessageService;
import ru.skillbox.auth_service.kafka.dto.KafkaMessageInputDto;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaMessageListener {

    private final KafkaMessageService kafkaMessageService;

    @Value("${app.kafka.kafkaMessageTopic1}")
    private String topicToRead;

    @Value("${app.kafla.kafkaMessageGroupId1}")
    private String messageGroupIdIdToRead;

    @KafkaListener(topics = "${app.kafka.kafkaMessageTopic0}",
            groupId = "${app.kafka.kafkaMessageGroupId0}",
            containerFactory = "kafkaMessageConcurrentKafkaListenerContainerFactory")
    public void receive(@Payload KafkaMessageInputDto message,
                        @Header(value = KafkaHeaders.RECEIVED_TOPIC) String topic) {

        log.info("Received message: {}", message);
        log.info("Message: {}; Topic: {}, Time: {}", message, topic, System.currentTimeMillis());

        System.out.println("message: " + message + " time received in ms:" + System.currentTimeMillis());
        kafkaMessageService.updateUserEntity(message);
    }
}
