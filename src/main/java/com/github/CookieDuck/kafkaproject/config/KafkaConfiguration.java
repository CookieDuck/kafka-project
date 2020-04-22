package com.github.CookieDuck.kafkaproject.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties("cards")
public class KafkaConfiguration {
    private Kafka kafka;
    private Topics topics;
    private Integer pollIntervalMs;

    @Data
    public static class Kafka {
        private List<String> serverList;
    }

    @Data
    public static class Topics {
        private String deck;
        private String top;
        private String bottom;
        private String shuffled;
        private String output;
    }
}
