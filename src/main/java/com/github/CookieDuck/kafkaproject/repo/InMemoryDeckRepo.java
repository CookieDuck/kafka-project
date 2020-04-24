package com.github.CookieDuck.kafkaproject.repo;

import com.github.CookieDuck.kafkaproject.model.Card;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class InMemoryDeckRepo implements DeckRepo {
    private final Map<Integer, DeckEntity> decks = new HashMap<>();
    private final Map<Integer, List<Card>> shufflesForDecks = new HashMap<>();
    private final Map<Integer, Integer> remainingShuffles = new HashMap<>();

    @Override
    public synchronized DeckEntity save(DeckEntity deck) {
        int id = generateId(deck);
        if (decks.get(id) != null) {
            throw new DuplicateKeyException("Already processing a request for a deck: " + deck);
        }
        DeckEntity entity = DeckEntity.builder()
            .id(id)
            .cards(deck.getCards())
            .build();
        decks.put(id, entity);
        return entity;
    }

    @Override
    public DeckEntity getById(int id) {
        return decks.get(id);
    }

    @Override
    public boolean deleteById(int id) {
        DeckEntity current = decks.get(id);
        if (current == null) {
            log.error("Error deleting: no deck exists by id: {}", id);
            return false;
        }
        log.info("Clearing deck data: {}", current);
        decks.put(id, null);
        return true;
    }

    @Override
    public List<Card> addShuffled(int id, List<Card> cards) {
        List<Card> current = shufflesForDecks.get(id);
        if (current == null) {
            current = new ArrayList<>();
        }
        current.addAll(cards);
        shufflesForDecks.put(id, current);
        return current;
    }

    @Override
    public void clearShuffled(int id) {
        shufflesForDecks.put(id, null);
    }

    @Override
    public void updateShufflesRemaining(int id, int shufflesRemaining) {
        remainingShuffles.put(id, shufflesRemaining);
    }

    @Override
    public int getShufflesRemaining(int id) {
        return remainingShuffles.get(id);
    }

    private static int generateId(DeckEntity entity) {
        return entity.getCards().hashCode();
    }
}
