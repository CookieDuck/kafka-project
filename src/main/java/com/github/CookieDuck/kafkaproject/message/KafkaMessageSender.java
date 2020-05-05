package com.github.CookieDuck.kafkaproject.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.CookieDuck.kafkaproject.repo.DeckEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Optional;

@Slf4j
public class KafkaMessageSender implements MessageSender<DeckEntity> {
    private final KafkaProducer<String, String> producer;
    private final String topic;
    private final ObjectMapper objectMapper;

    public KafkaMessageSender(
        KafkaProducer<String, String> producer,
        String topic,
        ObjectMapper objectMapper
    ) {
        this.producer = producer;
        this.topic = topic;
        this.objectMapper = objectMapper;
    }

    @Override
    public void send(DeckEntity message) {
        toRecord(topic, message).ifPresent(producer::send);
    }

    private Optional<ProducerRecord<String, String>> toRecord(String topic, DeckEntity deck) {
        String key = String.valueOf(deck.getId());
        try {
            return Optional.of(new ProducerRecord<>(topic, key, objectMapper.writeValueAsString(deck)));
        } catch (JsonProcessingException e) {
            log.error("Could not create record of deck: {} for topic: {}", deck, topic);
            return Optional.empty();
        }
    }
}
