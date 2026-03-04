package com.example.webflux;

import com.example.webflux.domain.TopicMessage;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@RestController
public class WebFluxController {

        private final KafkaTemplate<String, String> kafkaTemplate;
        private final AdminClient adminClient;

        public WebFluxController(KafkaTemplate<String, String> kafkaTemplate, AdminClient adminClient) {
                this.kafkaTemplate = kafkaTemplate;
                this.adminClient = adminClient;
        }

        @GetMapping("/writeDeveloper")
        public Mono<String> writeDeveloper() {
                String message = "Uditha - " + Instant.now().toString();
                try {
                        // block until send is acknowledged to ensure message is persisted
                        kafkaTemplate.send("developer", message).get();
                        kafkaTemplate.flush();
                        return Mono.just("sent: " + message);
                } catch (Exception e) {
                        return Mono.just("error sending: " + e.getMessage());
                }
        }

        @GetMapping("/readTopics")
        public Mono<List<TopicMessage>> readAllTopics() {
                return Mono.fromCallable(this::fetchTopicsAndMessages);
        }

        private List<TopicMessage> fetchTopicsAndMessages() throws Exception {
                List<TopicMessage> result = new ArrayList<>();

                // Get all topic names
                Set<String> topicNames = adminClient.listTopics().names().get();

                // Create a KafkaConsumer to read messages
                Properties consumerProps = new Properties();
                consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka:9092");
                consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "admin-reader-" + System.nanoTime());
                consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
                consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
                consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
                consumerProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 50); // Limit messages per topic
                consumerProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");

                try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProps)) {
                        for (String topic : topicNames) {
                                // Skip internal topics
                                if (topic.startsWith("__")) {
                                        continue;
                                }

                                consumer.subscribe(Collections.singletonList(topic));
                                List<String> messages = new ArrayList<>();

                                // Ensure we read from the beginning of the topic's partitions
                                consumer.poll(Duration.ofMillis(100)); // trigger partition assignment
                                if (!consumer.assignment().isEmpty()) {
                                        consumer.seekToBeginning(consumer.assignment());
                                }

                                // Poll for messages with timeout (try a few times)
                                int pollCount = 0;
                                while (messages.size() < 50 && pollCount < 5) {
                                        consumer.poll(Duration.ofSeconds(1))
                                                        .forEach(record -> messages.add(record.value()));
                                        pollCount++;
                                }

                                result.add(new TopicMessage(topic, messages));
                                consumer.unsubscribe();
                        }
                }

                return result;
        }
}
