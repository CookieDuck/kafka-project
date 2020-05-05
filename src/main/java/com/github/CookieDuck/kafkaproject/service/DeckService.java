package com.github.CookieDuck.kafkaproject.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.CookieDuck.kafkaproject.config.KafkaConfiguration;
import com.github.CookieDuck.kafkaproject.message.MessageBuilder;
import com.github.CookieDuck.kafkaproject.message.MessageSender;
import com.github.CookieDuck.kafkaproject.model.Card;
import com.github.CookieDuck.kafkaproject.repo.DeckEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.util.List;

@Slf4j
public class DeckService extends AbstractDeckEntityConsumer {
    private final MessageSender<DeckEntity> topSender;
    private final MessageSender<DeckEntity> bottomSender;

    public DeckService(
        KafkaConsumer<String, String> consumer,
        MessageSender<DeckEntity> topSender,
        MessageSender<DeckEntity> bottomSender,
        KafkaConfiguration config,
        ObjectMapper objectMapper
    ) {
        super(
            consumer,
            config.getTopics().getDeck(),
            config.getPollIntervalMs(),
            objectMapper
        );

        this.topSender = topSender;
        this.bottomSender = bottomSender;
    }

    @Override
    void processDeck(DeckEntity deck) {
        riffleShuffle(deck);
    }

    @Override
    String getName() {
        return "deck";
    }

    private void riffleShuffle(DeckEntity deck) {
        List<Card> topHalf = getTopHalf(deck.getCards());
        List<Card> bottomHalf = getBottomHalf(deck.getCards());
        log.debug("Top half has {} cards, bottom half has {} cards", topHalf.size(), bottomHalf.size());

        MessageBuilder messageBuilder = new MessageBuilder(deck);
        topSender.send(messageBuilder.createMessage(topHalf));
        bottomSender.send(messageBuilder.createMessage(bottomHalf));
    }

    private List<Card> getTopHalf(List<Card> cards) {
        return cards.subList(0, getHalfwayPoint(cards));
    }

    private List<Card> getBottomHalf(List<Card> cards) {
        return cards.subList(getHalfwayPoint(cards), cards.size());
    }

    private int getHalfwayPoint(List<Card> cards) {
        return Math.round(cards.size() / 2);
    }
}
