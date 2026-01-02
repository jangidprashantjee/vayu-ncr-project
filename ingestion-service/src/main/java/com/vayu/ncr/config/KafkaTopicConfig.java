package com.vayu.ncr.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic aqiTopic() {
        return TopicBuilder.name("raw-aqi-data")
                .partitions(3)  // ⚡ SPLIT DATA INTO 3 LANES (Great for scaling)
                .replicas(1)    // We only have 1 Broker in Docker, so 1 replica
                .build();
    }
}