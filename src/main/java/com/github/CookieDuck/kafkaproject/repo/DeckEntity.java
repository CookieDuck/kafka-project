package com.github.CookieDuck.kafkaproject.repo;

import com.github.CookieDuck.kafkaproject.model.Card;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Data
@Builder
@NoArgsConstructor(force = true)
@AllArgsConstructor
public class DeckEntity {
    private int id;
    private List<Card> cards;

    public int size() {
        return CollectionUtils.isEmpty(cards) ? 0 : cards.size();
    }
}
