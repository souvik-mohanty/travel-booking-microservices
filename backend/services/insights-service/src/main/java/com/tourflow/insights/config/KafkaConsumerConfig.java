package com.tourflow.insights.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import java.util.HashMap;
import java.util.Map;

// @EnableKafka is required here even though spring-kafka is on the
// classpath -- once this project defines its own ConsumerFactory/
// ConcurrentKafkaListenerContainerFactory beans, @KafkaListener processing
// doesn't start automatically (see docs/architecture/COMMUNICATION.md).
@Configuration
@EnableKafka
public class KafkaConsumerConfig {

    @Bean
    public ConsumerFactory<String, String> consumerFactory(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers,
            @Value("${spring.kafka.consumer.group-id}") String groupId,
            @Value("${spring.kafka.security.protocol:PLAINTEXT}") String securityProtocol,
            @Value("${spring.kafka.sasl.mechanism:PLAIN}") String saslMechanism,
            @Value("${spring.kafka.sasl.jaas-config:}") String saslJaasConfig
    ) {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        // New consumer group: start from the beginning of the topic so a
        // freshly built booking_event_log backfills from every
        // BookingConfirmed still retained on the topic.
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        // Blank/PLAINTEXT locally (see docker-compose.yml); set to SASL_SSL with
        // managed-Kafka credentials (Aiven, Redpanda, ...) in the "render" deployment, which has no
        // unauthenticated Kafka listener to connect to.
        config.put("security.protocol", securityProtocol);
        if (!saslJaasConfig.isBlank()) {
            config.put("sasl.mechanism", saslMechanism);
            config.put("sasl.jaas.config", saslJaasConfig);
        }
        return new DefaultKafkaConsumerFactory<>(config);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
            ConsumerFactory<String, String> consumerFactory
    ) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        return factory;
    }
}
