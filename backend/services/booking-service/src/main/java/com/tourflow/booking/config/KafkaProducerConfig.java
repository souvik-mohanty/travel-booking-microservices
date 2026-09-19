package com.tourflow.booking.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

// Producer side only -- booking-service publishes events, it doesn't
// consume any. Values are plain JSON strings (serialized by hand with the
// project's own tools.jackson ObjectMapper in BookingEventPublisher) rather
// than spring-kafka's JsonSerializer, which is built around the classic
// com.fasterxml.jackson.databind.ObjectMapper this project moved away from
// (see the Jackson 3 gotcha in docs/architecture/SECURITY.md).
@Configuration
public class KafkaProducerConfig {

    @Bean
    public ProducerFactory<String, String> producerFactory(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers,
            @Value("${spring.kafka.security.protocol:PLAINTEXT}") String securityProtocol,
            @Value("${spring.kafka.sasl.mechanism:PLAIN}") String saslMechanism,
            @Value("${spring.kafka.sasl.jaas-config:}") String saslJaasConfig
    ) {

        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        // Blank/PLAINTEXT locally (see docker-compose.yml); set to SASL_SSL with
        // Upstash-issued credentials in the "render" deployment, which has no
        // unauthenticated Kafka listener to connect to.
        config.put("security.protocol", securityProtocol);
        if (!saslJaasConfig.isBlank()) {
            config.put("sasl.mechanism", saslMechanism);
            config.put("sasl.jaas.config", saslJaasConfig);
        }

        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate(ProducerFactory<String, String> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }
}
