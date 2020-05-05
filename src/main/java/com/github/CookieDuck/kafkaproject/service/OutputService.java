package com.github.CookieDuck.kafkaproject.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.CookieDuck.kafkaproject.config.KafkaConfiguration;
import com.github.CookieDuck.kafkaproject.repo.DeckEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Optional;

@Slf4j
@Service
public class OutputService extends AbstractDeckEntityConsumer {
    private final SseEmitter sseEmitter;
    private final ObjectMapper objectMapper;

    @Autowired
    public OutputService(
        KafkaConsumer<String, String> consumer,
        KafkaConfiguration config,
        ObjectMapper objectMapper,
        SseEmitter sseEmitter
    ) {
        super(
            consumer,
            config.getTopics().getOutput(),
            config.getPollIntervalMs(),
            objectMapper
        );
        this.sseEmitter = sseEmitter;
        this.objectMapper = objectMapper;
    }

    @Override
    void processDeck(DeckEntity deck) {
        publishSSEEvent(deck);
    }

    @Override
    String getName() {
        return "output";
    }

    private void publishSSEEvent(DeckEntity deck) {
        log.debug("{} received {}", getConsumerTopic(), deck);

        String event;
        try {
            event = objectMapper.writeValueAsString(deck);
        } catch (JsonProcessingException e) {
            log.error("Unable to serialize deck: {}", deck);
            return;
        }

        try {
            sseEmitter.send(event);
        } catch (IOException e) {
            log.error("Unable to publish SSE for deck: {}", deck, e);
        }
    }
}
