package ru.skillbox.auth_service.kafka.configuration.config.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.common.serialization.Deserializer;
import ru.skillbox.common.events.CommonEvent;
import ru.skillbox.common.events.UserEvent;

import java.io.IOException;
import java.util.Map;

public class CommonEventDeserializer implements Deserializer<CommonEvent<?>> {

    private final ObjectMapper objectMapper;

    public CommonEventDeserializer() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // Регистрируем модуль для работы с Instant
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
    }

    @Override
    public CommonEvent<?> deserialize(String topic, byte[] data) {
        try {
            return objectMapper.readValue(data, new TypeReference<CommonEvent<UserEvent>>() {});
        } catch (IOException e) {
            throw new RuntimeException("Failed to deserialize CommonEvent", e);
        }
    }

    @Override
    public void close() {
    }
}

