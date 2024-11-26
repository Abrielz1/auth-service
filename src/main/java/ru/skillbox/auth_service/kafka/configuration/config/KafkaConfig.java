package ru.skillbox.auth_service.kafka.configuration.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import ru.skillbox.auth_service.kafka.configuration.config.util.CommonEventDeserializer;
import ru.skillbox.common.events.CommonEvent;
import ru.skillbox.common.events.RegUserEvent;
import ru.skillbox.common.events.UserEvent;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${app.kafka.kafkaMessageGroupId0}")
    private String kafkaMessageGroupId1;

    @Bean
    public ConsumerFactory<String, CommonEvent<UserEvent>> kafkaMessageConsumerFactory(ObjectMapper objectMapper) {

        Map<String, Object> config = new HashMap<>();

        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaMessageGroupId1);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, CommonEventDeserializer.class);
        config.put(JsonDeserializer.TRUSTED_PACKAGES, "*");

        return new DefaultKafkaConsumerFactory<>(config, new StringDeserializer(), new JsonDeserializer<>(objectMapper));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, CommonEvent<UserEvent>> kafkaMessageConcurrentKafkaListenerContainerFactory(
            ConsumerFactory<String, CommonEvent<UserEvent>> kafkaMessageConsumerFactory
    ) {

        ConcurrentKafkaListenerContainerFactory<String, CommonEvent<UserEvent>> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(kafkaMessageConsumerFactory);

        return factory;
    }

    @Bean
    public ProducerFactory<String, CommonEvent<RegUserEvent>> kafkaMessageProducerFactory(ObjectMapper objectMapper) {

        Map<String, Object> config = new HashMap<>();

        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        return new DefaultKafkaProducerFactory<>(config, new StringSerializer(), new JsonSerializer<>(objectMapper));
    }

    @Bean
    public KafkaTemplate<String, CommonEvent<RegUserEvent>> kafkaTemplate(ProducerFactory<String,
            CommonEvent<RegUserEvent>> kafkaMessageProducerFactory) {

        return new KafkaTemplate<>(kafkaMessageProducerFactory);
    }
}
