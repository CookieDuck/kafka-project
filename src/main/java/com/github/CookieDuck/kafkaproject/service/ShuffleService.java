package com.github.CookieDuck.kafkaproject.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.CookieDuck.kafkaproject.config.KafkaConfiguration;
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
@Service
public class ShuffleService extends AbstractDeckEntityConsumer {
    private final KafkaProducer<String, String> producer;
    private final DeckRepo deckRepo;
    private final String deckTopic;
    private final String outputTopic;

    @Autowired
    public ShuffleService(
        KafkaConsumer<String, String> consumer,
        KafkaProducer<String, String> producer,
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
        this.producer = producer;
        this.deckRepo = deckRepo;
        this.deckTopic = config.getTopics().getDeck();
        this.outputTopic = config.getTopics().getOutput();
    }

    @Override
    void processDeck(Optional<DeckEntity> maybeDeck) {
        maybeDeck.ifPresent((packet) -> {
            log.debug("{} topic got {}", super.getConsumerTopic(), packet);

            final int id = packet.getId();
            getParent(id).ifPresent((parent) -> {
                List<Card> accumulator = deckRepo.addShuffled(id, packet.getCards());
                int total = accumulator.size();
                log.debug("After adding {} cards, parent has {} total cards", packet.getCards().size(), total);
                if (total == parent.size()) {
                    log.debug("Got all the cards for id: {}", id);
                    sendToDeckOrOutput(id, accumulator);
                } else {
                    log.debug("Still waiting for {} more cards", parent.size() - total);
                }
            });
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

    private void sendToDeckOrOutput(int id, List<Card> cards) {
        int current = deckRepo.getShufflesRemaining(id);
        int remaining = current - 1;
        String destinationTopic;
        if (remaining > 0) {
            log.debug("There are still {} shuffles to do", remaining);
            destinationTopic = deckTopic;
        } else {
            destinationTopic = outputTopic;
            log.info("Completed shuffling, sending {} to topic: {}", cards, destinationTopic);
            deckRepo.deleteById(id);
        }

        deckRepo.updateShufflesRemaining(id, remaining);
        deckRepo.clearShuffled(id);
        DeckEntity shuffled = DeckEntity.builder()
            .id(id)
            .cards(cards)
            .build();
        toRecord(destinationTopic, shuffled).ifPresent(producer::send);
    }
}
