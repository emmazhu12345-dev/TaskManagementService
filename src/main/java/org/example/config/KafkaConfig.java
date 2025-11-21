package org.example.config;

import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.example.kafka.event.TaskEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

/**
 * Kafka configuration for producing and consuming TaskEvent messages. Uses JsonSerializer /
 * JsonDeserializer for serialization. Bootstrap servers are injected from application-*.yml.
 */
@Configuration
@EnableKafka
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    // =======================================================
    // Producer Configuration (String key, TaskEvent value)
    // =======================================================
    @Bean
    public ProducerFactory<String, TaskEvent> taskEventProducerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        // Optional: enable idempotent producers for stronger delivery guarantees
        // props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        // props.put(ProducerConfig.ACKS_CONFIG, "all");

        return new DefaultKafkaProducerFactory<>(props);
    }

    /**
     * the Spring Boot Kafka Producer client. It is the object we use to publish TaskEvent messages to
     * Kafka topics.
     */
    @Bean
    public KafkaTemplate<String, TaskEvent> taskEventKafkaTemplate() {
        return new KafkaTemplate<>(taskEventProducerFactory());
    }

    // =======================================================
    // Consumer Configuration (String key, TaskEvent value)
    // =======================================================
    @Bean
    public ConsumerFactory<String, TaskEvent> taskEventConsumerFactory(
            @Value("${spring.kafka.consumer.group-id:tms-default-group}") String groupId) {
        JsonDeserializer<TaskEvent> jsonDeserializer = new JsonDeserializer<>(TaskEvent.class);
        jsonDeserializer.addTrustedPackages("org.example.kafka.event");

        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, jsonDeserializer);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), jsonDeserializer);
    }

    /**
     * Kafka listener factory for @KafkaListener(containerFactory = "taskEventListenerFactory").
     * Handles automatic JSON → TaskEvent deserialization.
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, TaskEvent> taskEventListenerFactory(
            ConsumerFactory<String, TaskEvent> taskEventConsumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, TaskEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(taskEventConsumerFactory);

        // To use MANUAL ack mode in the future:
        // factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);

        return factory;
    }
}
