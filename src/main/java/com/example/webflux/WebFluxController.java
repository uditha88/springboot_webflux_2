package com.example.webflux;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.Instant;

@RestController
public class WebFluxController {

        private final KafkaTemplate<String, String> kafkaTemplate;

        public WebFluxController(KafkaTemplate<String, String> kafkaTemplate) {
                this.kafkaTemplate = kafkaTemplate;
        }

        @GetMapping("/writeDeveloper")
        public Mono<String> writeDeveloper() {
                String message = "Uditha - " + Instant.now().toString();
                kafkaTemplate.send("developer", message);
                return Mono.just("sent: " + message);
        }
}
