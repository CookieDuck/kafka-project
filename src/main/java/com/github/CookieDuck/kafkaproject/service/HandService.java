package com.github.CookieDuck.kafkaproject.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.CookieDuck.kafkaproject.config.KafkaConfiguration;
import com.github.CookieDuck.kafkaproject.model.Card;
import com.github.CookieDuck.kafkaproject.repo.DeckEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;

@Slf4j
@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class HandService extends AbstractDeckEntityConsumer {
    private final KafkaProducer<String, String> producer;
    private final String producerTopic;

    public HandService(
        KafkaConsumer<String, String> consumer,
        KafkaProducer<String, String> producer,
        KafkaConfiguration config,
        ObjectMapper objectMapper,
        String consumerTopic
    ) {
        super(
            consumer,
            consumerTopic,
            config.getPollIntervalMs(),
            objectMapper
        );
        this.producer = producer;
        this.producerTopic = config.getTopics().getShuffled();
    }

    @Override
    void processDeck(Optional<DeckEntity> maybeDeck) {
        maybeDeck.ifPresent((deck) -> {
            List<Card> allCards = deck.getCards();
            log.debug("{} got {} cards", super.getConsumerTopic(), allCards.size());
            while (!allCards.isEmpty()) {
                List<Card> front = takeRandomCardsFromFront(allCards);
                allCards = allCards.subList(front.size(), allCards.size());
                log.debug("{} took {} cards, so now {} remain", super.getConsumerTopic(), front.size(), allCards.size());
                DeckEntity portion = DeckEntity.builder()
                    .id(deck.getId())
                    .cards(front)
                    .build();

                toRecord(producerTopic, portion).ifPresent(producer::send);
            }
            log.debug("{} finished sending its cards", super.getConsumerTopic());
        });
    }

    @Override
    String getName() {
        return String.format("hand (%s)", getConsumerTopic());
    }

    /**
     * Returns a small subset of Cards from the front of the given list.
     * @param cards  Input to pull from.  Not mutated.
     * @return  Small subset of cards, taken sequentially from front of input list.
     */
    private List<Card> takeRandomCardsFromFront(List<Card> cards) {
        if (CollectionUtils.isEmpty(cards)) {
            return emptyList();
        }

        // Tend to favor 1 card.  Sometimes 2, rarely 3
        double diceRoll = Math.random() * 10;
        int nCards = 1;
        if (diceRoll > 5) {
            nCards = 2;
        }
        if (diceRoll > 8) {
            nCards = 3;
        }
        nCards = Math.min(nCards, cards.size());

        return cards.subList(0, nCards);
    }
}
