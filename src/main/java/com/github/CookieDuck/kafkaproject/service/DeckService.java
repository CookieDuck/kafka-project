package com.github.CookieDuck.kafkaproject.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.CookieDuck.kafkaproject.config.KafkaConfiguration;
import com.github.CookieDuck.kafkaproject.model.Card;
import com.github.CookieDuck.kafkaproject.repo.DeckEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class DeckService extends AbstractDeckEntityConsumer {
    private final KafkaProducer<String, String> producer;
    private final String topTopic;
    private final String bottomTopic;

    @Autowired
    public DeckService(
        KafkaConsumer<String, String> consumer,
        KafkaProducer<String, String> producer,
        KafkaConfiguration config,
        ObjectMapper objectMapper
    ) {
        super(
            consumer,
            config.getTopics().getDeck(),
            config.getPollIntervalMs(),
            objectMapper
        );
        this.producer = producer;
        this.topTopic = config.getTopics().getTop();
        this.bottomTopic = config.getTopics().getBottom();
    }

    @Override
    void processDeck(Optional<DeckEntity> maybeDeck) {
        maybeDeck.ifPresent((deck) -> {
            List<Card> cards = deck.getCards();
            int nCards = cards.size();

            int half = Math.round(nCards / 2);
            List<Card> topHalf = cards.subList(0, half);
            List<Card> bottomHalf = cards.subList(half, cards.size());
            log.debug("Top half has {} cards, bottom half has {} cards", topHalf.size(), bottomHalf.size());

            DeckEntity top = DeckEntity.builder()
                .id(deck.getId())
                .cards(topHalf)
                .build();
            DeckEntity bottom = DeckEntity.builder()
                .id(deck.getId())
                .cards(bottomHalf)
                .build();
            toRecord(topTopic, top).ifPresent(producer::send);
            toRecord(bottomTopic, bottom).ifPresent(producer::send);
        });
    }

    @Override
    String getName() {
        return "deck";
    }
}
