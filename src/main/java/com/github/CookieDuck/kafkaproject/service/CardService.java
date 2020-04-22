package com.github.CookieDuck.kafkaproject.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.CookieDuck.kafkaproject.config.KafkaConfiguration;
import com.github.CookieDuck.kafkaproject.model.ShuffleRequest;
import com.github.CookieDuck.kafkaproject.repo.DeckEntity;
import com.github.CookieDuck.kafkaproject.repo.DeckRepo;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class CardService {
    private final DeckRepo deckRepo;
    private final KafkaProducer<String, String> producer;
    private final ObjectMapper objectMapper;
    private final String topic;

    @Autowired
    public CardService(
        DeckRepo deckRepo,
        KafkaProducer<String, String> producer,
        KafkaConfiguration config,
        ObjectMapper objectMapper
    ) {
        this.deckRepo = deckRepo;
        this.producer = producer;
        this.objectMapper = objectMapper;
        this.topic = config.getTopics().getDeck();
    }

    public void shuffle(ShuffleRequest request) {
        Optional<DeckEntity> maybeDeck = persistRequest(request);
        maybeDeck.ifPresent(this::sendDeckMessage);
    }

    private Optional<DeckEntity> persistRequest(ShuffleRequest request) {
        log.info("Received request {}", request);
        DeckEntity deck;
        try {
            deck = deckRepo.save(fromRequest(request));
        } catch (DuplicateKeyException dae) {
            log.warn("Already processing a request for this deck; ignoring");
            return Optional.empty();
        }
        int id = deck.getId();
        log.info("Deck id from request: {}", id);
        deckRepo.updateShufflesRemaining(id, request.getTimes());
        return Optional.of(deck);
    }

    private void sendDeckMessage(DeckEntity deck) {
        try {
            producer.send(asRecord(deck));
        } catch (JsonProcessingException e) {
            log.error("Could not send (to topic {}): message: {}", topic, deck, e);
        }
    }

    private static DeckEntity fromRequest(ShuffleRequest request) {
        return DeckEntity.builder()
            .cards(request.getCards())
            .build();
    }

    private ProducerRecord<String, String> asRecord(DeckEntity deck) throws JsonProcessingException {
        String key = String.valueOf(deck.getId());
        String value = objectMapper.writeValueAsString(deck);
        return new ProducerRecord<>(topic, key, value);
    }
}
