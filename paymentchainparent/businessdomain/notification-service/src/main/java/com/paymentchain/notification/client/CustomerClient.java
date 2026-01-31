package com.paymentchain.notification.client;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
public class CustomerClient {

    private static final Logger log = LoggerFactory.getLogger(CustomerClient.class);

    @Autowired
    @Qualifier("customerWebClient")
    private WebClient customerWebClient;

    /**
     * Returns customer's email as Mono<String> or empty Mono on error/not found.
     */
    public Mono<String> getEmailById(Long id) {
        return customerWebClient.get()
                .uri(uriBuilder -> uriBuilder.path("/customers/{id}").build(id))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(node -> node.hasNonNull("email") ? node.get("email").asText(null) : null)
                .timeout(Duration.ofSeconds(2))
                .doOnError(e -> log.warn("Error fetching customer email {}: {}", id, e.getMessage()));
    }
}
