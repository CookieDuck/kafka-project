package com.github.CookieDuck.kafkaproject.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.CookieDuck.kafkaproject.model.Message;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

@Slf4j
@Service
public class ProducerService {
    private final KafkaProducer<String, String> producer;
    private final ObjectMapper objectMapper;

    @Value("${producer.topic}")
    private String topic;

    @Autowired
    public ProducerService(
        KafkaProducer<String, String> producer,
        ObjectMapper objectMapper
    ) {
        this.producer = producer;
        this.objectMapper = objectMapper;
    }

    public void send(Message message) {
        try {
            producer.send(asRecord(message)).get();
        } catch (InterruptedException e) {
            log.error("Interrupted during send", e);
        } catch (ExecutionException e) {
            log.error("Execution Exception during send", e);
        } catch (JsonProcessingException e) {
            log.error("Could not deserialize message: {}", message, e);
        }
        producer.flush();
    }

    private ProducerRecord<String, String> asRecord(Message message) throws JsonProcessingException {
        String key = message.getId();
        String value = objectMapper.writeValueAsString(message);
        return new ProducerRecord<>(topic, key, value);
    }
}
