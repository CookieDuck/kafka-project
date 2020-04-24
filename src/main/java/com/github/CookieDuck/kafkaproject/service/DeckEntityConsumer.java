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
    }

    @Override
    public void run() {
        try {
            while (true) {
                ConsumerRecords<String, String> records =
                    this.consumer.poll(Duration.ofMillis(this.pollInterval));
                log.debug("Received {} records after poll of topic: {}", records.count(), this.consumerTopic);
                records.forEach(this.consumingFunction);
            }
        } catch (WakeupException wakeup) {
            log.debug("Received WakeupException; shutting down");
        } finally {
            log.debug("Closing consumer for topic: {}", this.consumerTopic);
            this.consumer.close();
        }
    }

    private void subscribe() {
        log.debug("Subscribing to {}", this.consumerTopic);
        this.consumer.subscribe(singletonList(this.consumerTopic));

        log.debug("Starting consumer thread");
        Thread thread = new Thread(this);
        thread.start();
    }

    void shutdown() {
        log.debug("Shutdown called");
        this.consumer.wakeup();
    }
}
