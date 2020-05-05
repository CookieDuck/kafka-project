package com.github.CookieDuck.kafkaproject.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.CookieDuck.kafkaproject.message.KafkaMessageSender;
import com.github.CookieDuck.kafkaproject.message.MessageSender;
import com.github.CookieDuck.kafkaproject.repo.DeckEntity;
import com.github.CookieDuck.kafkaproject.repo.DeckRepo;
import com.github.CookieDuck.kafkaproject.repo.InMemoryDeckRepo;
import com.github.CookieDuck.kafkaproject.service.DeckService;
import com.github.CookieDuck.kafkaproject.service.HandService;
import com.github.CookieDuck.kafkaproject.service.ShuffleService;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Properties;

@Configuration
public class BeanFactory {
    private static final String SERIALIZER_NAME = StringSerializer.class.getName();
    private static final String DESERIALIZER_NAME = StringDeserializer.class.getName();
    private static final String CONSUMER_AUTO_OFFSET = "earliest";
    private static final String GROUP_ID = "consumers";

    private final KafkaConfiguration config;

    @Autowired
    public BeanFactory(KafkaConfiguration config) {
        this.config = config;
    }

    @Bean
    KafkaProducer<String, String> producer() {
        return new KafkaProducer<>(configureProducer());
    }

    /*
     * Because KafkaConsumer is not thread safe, this bean needs to be
     * declared with the scope prototype.  The application fails to load
     * if multiple threads try to access the same consumer.
     */
    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    KafkaConsumer<String, String> consumer() {
        return new KafkaConsumer<>(configureConsumer());
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public DeckRepo deckRepo() {
        return new InMemoryDeckRepo();
    }

    @Bean
    public SseEmitter sseEmitter() {
        return new SseEmitter(Long.MAX_VALUE);
    }

    @Bean
    public DeckService deckService(
        KafkaConsumer<String, String> consumer,
        KafkaProducer<String, String> producer,
        ObjectMapper objectMapper
    ) {
        String topTopic = config.getTopics().getTop();
        KafkaMessageSender topSender = new KafkaMessageSender(producer, topTopic, objectMapper);
        String bottomTopic = config.getTopics().getBottom();
        KafkaMessageSender bottomSender = new KafkaMessageSender(producer, bottomTopic, objectMapper);

        return new DeckService(
            consumer,
            topSender,
            bottomSender,
            config,
            objectMapper
        );
    };

    @Bean
    public HandService top(
        KafkaConsumer<String, String> consumer,
        MessageSender<DeckEntity> shuffledSender,
        ObjectMapper objectMapper
    ) {
        return new HandService(
            consumer,
            shuffledSender,
            config,
            objectMapper,
            config.getTopics().getTop()
        );
    }

    @Bean
    public HandService bottom(
        KafkaConsumer<String, String> consumer,
        MessageSender<DeckEntity> shuffledSender,
        ObjectMapper objectMapper
    ) {
        return new HandService(
            consumer,
            shuffledSender,
            config,
            objectMapper,
            config.getTopics().getBottom()
        );
    }

    @Bean
    MessageSender<DeckEntity> shuffledMessageSender(
        KafkaProducer<String, String> producer,
        ObjectMapper objectMapper
    ) {
        String topic = config.getTopics().getShuffled();
        return new KafkaMessageSender(producer, topic, objectMapper);
    }

    @Bean
    ShuffleService shuffleService(
        KafkaConsumer<String, String> consumer,
        KafkaProducer<String, String> producer,
        ObjectMapper objectMapper,
        DeckRepo deckRepo
    ) {
        String deckTopic = config.getTopics().getDeck();
        MessageSender<DeckEntity> deckSender = new KafkaMessageSender(producer, deckTopic, objectMapper);
        String outputTopic = config.getTopics().getOutput();
        MessageSender<DeckEntity> outputSender = new KafkaMessageSender(producer, outputTopic, objectMapper);

        return new ShuffleService(
            consumer,
            deckSender,
            outputSender,
            config,
            objectMapper,
            deckRepo
        );
    }

    private Properties configureProducer() {
        Properties properties = new Properties();
        properties.put(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
            getServerList()
        );
        properties.put(
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
            SERIALIZER_NAME
        );
        properties.put(
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
            SERIALIZER_NAME
        );
        return properties;
    }

    private Properties configureConsumer() {
        Properties properties = new Properties();
        properties.put(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
            getServerList()
        );
        properties.put(
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
            DESERIALIZER_NAME
        );
        properties.put(
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
            DESERIALIZER_NAME
        );
        properties.put(
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
            CONSUMER_AUTO_OFFSET
        );
        properties.put(
            ConsumerConfig.GROUP_ID_CONFIG,
            GROUP_ID
        );
        return properties;
    }

    private String getServerList() {
        return StringUtils.collectionToCommaDelimitedString(config.getKafka().getServerList());
    }
}
