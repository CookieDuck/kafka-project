package com.github.CookieDuck.kafkaproject.message;

import com.github.CookieDuck.kafkaproject.model.Card;
import com.github.CookieDuck.kafkaproject.repo.DeckEntity;

import java.util.List;

public class MessageBuilder {
    private final DeckEntity sourceDeck;

    public MessageBuilder(DeckEntity sourceDeck) {
        this.sourceDeck = sourceDeck;
    }

    public DeckEntity createMessage(List<Card> cardsForMessage) {
        return DeckEntity.builder().id(this.sourceDeck.getId()).cards(cardsForMessage).build();
    }
}
