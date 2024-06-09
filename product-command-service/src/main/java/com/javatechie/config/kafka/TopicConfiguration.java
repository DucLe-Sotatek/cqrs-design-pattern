package com.javatechie.config.kafka;


import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TopicConfiguration {

    @Bean
    public NewTopic syncTopic() {
        return new NewTopic("product_sync_all_v1", 1, (short) 1);
    }

    @Bean
    public NewTopic prodEventTopic() {
        return new NewTopic("product-event-topic", 1, (short) 1);
    }
}
