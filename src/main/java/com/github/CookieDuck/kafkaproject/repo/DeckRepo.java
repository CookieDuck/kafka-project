package com.github.CookieDuck.kafkaproject.repo;

import com.github.CookieDuck.kafkaproject.model.Card;

import java.util.List;

public interface DeckRepo {
    DeckEntity save(DeckEntity deck);
    DeckEntity getById(int id);
    boolean deleteById(int id);

    /*
     * Real repo probably wouldn't have methods like this.  These are for
     * the shuffle consumer to accumulate messages, and to clear them when
     * full.
     */
    List<Card> addShuffled(int id, List<Card> cards);
    void clearShuffled(int id);

    // probably should be in their own repo, but since it's a 'demo' project...
    void updateShufflesRemaining(int id, int shufflesRemaining);
    int getShufflesRemaining(int id);
}
