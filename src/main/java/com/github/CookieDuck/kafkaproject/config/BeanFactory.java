package com.github.CookieDuck.kafkaproject.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.CookieDuck.kafkaproject.repo.DeckRepo;
import com.github.CookieDuck.kafkaproject.repo.InMemoryDeckRepo;
import com.github.CookieDuck.kafkaproject.service.HandService;
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

        return new KafkaProducer<>(properties);
    }

    /*
     * Because KafkaConsumer is not thread safe, this bean needs to be
     * declared with the scope prototype.  The application fails to load
     * if multiple threads try to access the same consumer.
     */
    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    KafkaConsumer<String, String> consumer() {
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

        return new KafkaConsumer<>(properties);
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
    public HandService top(
        KafkaConsumer<String, String> consumer,
        KafkaProducer<String, String> producer,
        ObjectMapper objectMapper
    ) {
        return new HandService(
            consumer,
            producer,
            config,
            objectMapper,
            config.getTopics().getTop()
        );
    }

    @Bean
    public HandService bottom(
        KafkaConsumer<String, String> consumer,
        KafkaProducer<String, String> producer,
        ObjectMapper objectMapper
    ) {
        return new HandService(
            consumer,
            producer,
            config,
            objectMapper,
            config.getTopics().getBottom()
        );
    }

    private String getServerList() {
        return StringUtils.collectionToCommaDelimitedString(config.getKafka().getServerList());
    }
}
