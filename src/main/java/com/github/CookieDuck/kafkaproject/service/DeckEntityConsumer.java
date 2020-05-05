package com.github.CookieDuck.kafkaproject.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;

import java.time.Duration;
import java.util.function.Consumer;

import static java.util.Collections.singletonList;

@Slf4j
public class DeckEntityConsumer implements Runnable {
    private final KafkaConsumer<String, String> consumer;
    private final Consumer<ConsumerRecord<String, String>> consumingFunction;
    private final String consumerTopic;
    private final int pollInterval;

    private boolean listeningForMessages = true;

    DeckEntityConsumer(
        KafkaConsumer<String, String> consumer,
        Consumer<ConsumerRecord<String, String>> consumingFunction,
        String consumerTopic,
        int pollInterval
    ) {
        this.consumer = consumer;
        this.consumingFunction = consumingFunction;
        this.consumerTopic = consumerTopic;
        this.pollInterval = pollInterval;

        this.subscribe();
        this.startConsumingThread();
    }

    @Override
    public void run() {
        try {
            processDeckRecords();
        } catch (WakeupException wakeup) {
            log.debug("Received WakeupException; shutting down");
        } finally {
            log.debug("Closing consumer for topic: {}", this.consumerTopic);
            this.consumer.close();
        }
    }

    String getConsumerTopic() {
        return this.consumerTopic;
    }

    private void processDeckRecords() {
        while (this.listeningForMessages) {
            ConsumerRecords<String, String> records =
                this.consumer.poll(Duration.ofMillis(this.pollInterval));
            log.debug("Received {} records after poll of topic: {}", records.count(), this.consumerTopic);
            records.forEach(this.consumingFunction);
        }
    }

    void shutdown() {
        log.debug("Shutdown called");
        this.listeningForMessages = false;
        this.consumer.wakeup();
    }

    private void subscribe() {
        log.debug("Subscribing to {}", this.consumerTopic);
        this.consumer.subscribe(singletonList(this.consumerTopic));
    }

    private void startConsumingThread() {
        log.debug("Starting consumer thread");
        Thread thread = new Thread(this);
        thread.start();
    }
}
