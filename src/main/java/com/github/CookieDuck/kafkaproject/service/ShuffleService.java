package com.github.CookieDuck.kafkaproject.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.CookieDuck.kafkaproject.config.KafkaConfiguration;
import com.github.CookieDuck.kafkaproject.message.KafkaMessageSender;
import com.github.CookieDuck.kafkaproject.message.MessageBuilder;
import com.github.CookieDuck.kafkaproject.message.MessageSender;
import com.github.CookieDuck.kafkaproject.model.Card;
import com.github.CookieDuck.kafkaproject.repo.DeckEntity;
import com.github.CookieDuck.kafkaproject.repo.DeckRepo;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
public class ShuffleService extends AbstractDeckEntityConsumer {
    private final DeckRepo deckRepo;
    private final MessageSender<DeckEntity> deckSender;
    private final MessageSender<DeckEntity> outputSender;

    @Autowired
    public ShuffleService(
        KafkaConsumer<String, String> consumer,
        MessageSender<DeckEntity> deckSender,
        MessageSender<DeckEntity> outputSender,
        KafkaConfiguration config,
        ObjectMapper objectMapper,
        DeckRepo deckRepo
    ) {
        super(
            consumer,
            config.getTopics().getShuffled(),
            config.getPollIntervalMs(),
            objectMapper
        );
        this.deckRepo = deckRepo;

        this.deckSender = deckSender;
        this.outputSender = outputSender;
    }

    @Override
    void processDeck(DeckEntity packet) {
        log.debug("{} topic got {}", super.getConsumerTopic(), packet);

        final int id = packet.getId();
        getParent(id).ifPresent((parent) -> {
            Pile pile = from(parent);
            pile.add(packet.getCards());

            if (pile.isAllCardsPresent()) {
                sendMessage(pile);
                pile.updateShuffleStatus();
            }
        });
    }

    @Override
    String getName() {
        return "shuffle";
    }

    private Optional<DeckEntity> getParent(int id) {
        DeckEntity parent = deckRepo.getById(id);
        if (parent == null) {
            log.error("No parent found for deck by id: {}", id);
        }
        return Optional.ofNullable(parent);
    }

    private void sendMessage(Pile pile) {
        MessageSender<DeckEntity> sender = getSender(pile.getRemainingShuffles());
        MessageBuilder messageBuilder = new MessageBuilder(pile.getParent());
        DeckEntity message = messageBuilder.createMessage(pile.getCards());
        sender.send(message);
    }

    private MessageSender<DeckEntity> getSender(int remainingShuffles) {
        if (remainingShuffles > 0) {
            log.debug("There are still {} shuffles to do", remainingShuffles);
            return deckSender;
        }
        log.debug("There are no more shuffles remaining");
        return outputSender;
    }

    private Pile from(DeckEntity parent) {
        return new Pile(deckRepo, parent);
    }

    private static class Pile {
        private final DeckRepo deckRepo;
        private final DeckEntity parent;

        private List<Card> cards;

        private Pile(DeckRepo deckRepo, DeckEntity parent) {
            this.deckRepo = deckRepo;
            this.parent = parent;
        }

        DeckEntity getParent() {
            return parent;
        }

        void add(List<Card> input) {
            cards = deckRepo.addShuffled(parent.getId(), input);
            log.debug("After adding {} cards, parent has {} total cards", input.size(), getSize());
        }

        List<Card> getCards() {
            return cards;
        }

        boolean isAllCardsPresent() {
            boolean isAllPresent = parent.size() == getSize();
            if (isAllPresent) {
                log.debug("Got all the cards for id: {}", parent.getId());
            } else {
                log.debug("Still waiting for {} more cards", parent.size() - getSize());
            }
            return isAllPresent;
        }

        void updateShuffleStatus() {
            final int remaining = getRemainingShuffles();
            final int id = parent.getId();

            deckRepo.updateShufflesRemaining(id, remaining);
            deckRepo.clearShuffled(id);

            if (remaining == 0) {
                deckRepo.deleteById(id);
            }
        }

        int getRemainingShuffles() {
            return deckRepo.getShufflesRemaining(parent.getId()) - 1;
        }

        private int getSize() {
            return cards != null ? cards.size() : 0;
        }
    }
}
