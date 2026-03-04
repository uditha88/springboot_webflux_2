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
                kafkaTemplate.send("developer", message);
                return Mono.just("sent: " + message);
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
                consumerProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 10); // Limit messages per topic

                try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProps)) {
                        for (String topic : topicNames) {
                                // Skip internal topics
                                if (topic.startsWith("__")) {
                                        continue;
                                }

                                consumer.subscribe(Collections.singletonList(topic));
                                List<String> messages = new ArrayList<>();

                                // Poll for messages with timeout
                                int pollCount = 0;
                                while (messages.size() < 10 && pollCount < 3) {
                                        for (ConsumerRecord<String, String> record : consumer
                                                        .poll(Duration.ofSeconds(1))) {
                                                messages.add(record.value());
                                        }
                                        pollCount++;
                                }

                                result.add(new TopicMessage(topic, messages));
                                consumer.unsubscribe();
                        }
                }

                return result;
        }
}
