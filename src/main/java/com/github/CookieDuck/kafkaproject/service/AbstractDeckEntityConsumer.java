package com.github.CookieDuck.kafkaproject.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.CookieDuck.kafkaproject.repo.DeckEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.util.StringUtils;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.Optional;
import java.util.function.Consumer;

@Slf4j
public abstract class AbstractDeckEntityConsumer
    implements Consumer<ConsumerRecord<String, String>> {
    private final DeckEntityConsumer deckEntityConsumer;
    private final ObjectMapper objectMapper;

    public AbstractDeckEntityConsumer(
        KafkaConsumer<String, String> consumer,
        String consumerTopic,
        Integer pollInterval,
        ObjectMapper objectMapper
    ) {
        this.deckEntityConsumer = new DeckEntityConsumer(
            consumer,
            this,
            consumerTopic,
            pollInterval
        );
        this.objectMapper = objectMapper;
    }

    abstract void processDeck(DeckEntity deck);

    abstract String getName();

    @Override
    public void accept(ConsumerRecord<String, String> record) {
        fromRecord(record).ifPresent(this::processDeck);
    }

    @Override
    public Consumer<ConsumerRecord<String, String>> andThen(Consumer<? super ConsumerRecord<String, String>> after) {
        log.warn("andThen called.  Wasn't expecting that.");
        return null;
    }

    @PreDestroy
    public void shutdown() {
        log.debug("Shutting down {}", getName());
        deckEntityConsumer.shutdown();
    }

    String getConsumerTopic() {
        return deckEntityConsumer.getConsumerTopic();
    }

    private Optional<DeckEntity> fromRecord(ConsumerRecord<String, String> record) {
        if (record == null || StringUtils.isEmpty(record.value())) {
            return Optional.empty();
        }

        try {
            return Optional.ofNullable(objectMapper.readValue(record.value(), DeckEntity.class));
        } catch (IOException ioe) {
            log.error("Could not parse record {} from topic: {}", record.value(), this.getConsumerTopic());
            return Optional.empty();
        }
    }
}
