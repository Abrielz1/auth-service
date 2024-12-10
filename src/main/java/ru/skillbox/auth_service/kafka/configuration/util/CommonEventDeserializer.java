package ru.skillbox.auth_service.kafka.configuration.util;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Deserializer;
import ru.skillbox.auth_service.exception.exceptions.BadRequestException;
import ru.skillbox.common.events.CommonEvent;
import ru.skillbox.common.events.RegUserEvent;
import ru.skillbox.common.events.UserEvent;

import java.io.IOException;
import java.util.Map;

@Slf4j
public class CommonEventDeserializer implements Deserializer<CommonEvent<?>> {

    private final ObjectMapper objectMapper;

    public CommonEventDeserializer() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        log.info("Nothing here");
    }

    @Override
    public CommonEvent<?> deserialize(String topic, byte[] data) {
        try {
            if (topic.equals("user-events")) {
                JavaType javaType = objectMapper.getTypeFactory().constructParametricType(CommonEvent.class, UserEvent.class);
                return objectMapper.readValue(data, javaType);
            } else if (topic.equals("registration_sending")) {
                JavaType javaType = objectMapper.getTypeFactory().constructParametricType(CommonEvent.class, RegUserEvent.class);
                return objectMapper.readValue(data, javaType);
            } else {
                throw new IllegalArgumentException("Unknown topic: " + topic);
            }
        } catch (IOException e) {
            throw new BadRequestException("Failed to deserialize CommonEvent " + e);
        }
    }

    @Override
    public void close() {
        log.info("Nothing here");
    }
}
