package com.paymentchain.notification.client;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

@Component
public class CustomerClient {

    private static final Logger log = LoggerFactory.getLogger(CustomerClient.class);

    @Autowired
    @Qualifier("customerWebClient")
    private WebClient customerWebClient;

    @Autowired
    private DiscoveryClient discoveryClient;

    @Value("${api.gateway.url:http://api-gateway:8080}")
    private String apiGatewayUrl;

    /**
     * Returns customer's email as Mono<String> or empty Mono on error/not found.
     * Implements retries with exponential backoff for transient network errors and
     * returns an empty Mono on 404 or exhausted retries. If the primary call
     * fails with connection errors, attempts to resolve instances via DiscoveryClient
     * and call them directly. As a last resort it will try the API gateway.
     */
    public Mono<String> getEmailById(Long id) {
        return customerWebClient.get()
                .uri(uriBuilder -> uriBuilder.path("/customers/{id}").build(id))
                .exchangeToMono(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        return response.bodyToMono(JsonNode.class)
                                .map(node -> node.hasNonNull("email") ? node.get("email").asText(null) : null);
                    }
                    if (response.statusCode().value() == 404) {
                        // Not found -> return empty
                        return Mono.empty();
                    }
                    // For other statuses, propagate error to trigger retry logic
                    return response.createException().flatMap(Mono::error);
                })
                .timeout(Duration.ofSeconds(3))
                .retryWhen(Retry.backoff(3, Duration.ofMillis(300))
                        .filter(throwable -> {
                            // Retry on transport / IO type errors
                            return (throwable instanceof WebClientRequestException) || (throwable instanceof IOException);
                        })
                        .onRetryExhaustedThrow((spec, rs) -> rs.failure()))
                .doOnError(e -> log.warn("Error fetching customer email {}: {}", id, e.toString()))
                .onErrorResume(e -> tryDiscoveryAndGatewayFallback(id, e));
    }

    private Mono<String> tryDiscoveryAndGatewayFallback(Long id, Throwable cause) {
        // Discovery fallback
        try {
            List<ServiceInstance> instances = discoveryClient.getInstances("businessdomain-customer");
            log.warn("CustomerClient discovery fallback: found {} instances (cause={})", instances == null ? 0 : instances.size(), cause == null ? "-" : cause.toString());
            if (instances != null && !instances.isEmpty()) {
                return reactor.core.publisher.Flux.fromIterable(instances)
                        .concatMap(inst -> {
                            String url = (inst.isSecure() ? "https://" : "http://") + inst.getHost() + ":" + inst.getPort();
                            log.info("CustomerClient fallback: trying resolved instance {} for customer {}", url, id);
                            WebClient direct = WebClient.builder().baseUrl(url).build();
                            return direct.get()
                                    .uri("/customers/{id}", id)
                                    .retrieve()
                                    .bodyToMono(JsonNode.class)
                                    .map(node -> node.hasNonNull("email") ? node.get("email").asText(null) : null)
                                    .timeout(Duration.ofSeconds(2))
                                    .onErrorResume(inner -> {
                                        log.warn("CustomerClient fallback failed for instance {}: {}", url, inner.toString());
                                        return Mono.empty();
                                    });
                        })
                        .filter(email -> email != null && !email.isEmpty())
                        .doOnNext(email -> log.info("CustomerClient discovery fallback succeeded for id {} -> {}", id, email))
                        .next();
            }
        } catch (Exception ex) {
            log.warn("Discovery fallback failed while fetching customer {}: {}", id, ex.toString());
        }

        // Last resort: try API gateway
        try {
            String gateway = apiGatewayUrl.endsWith("/") ? apiGatewayUrl.substring(0, apiGatewayUrl.length() - 1) : apiGatewayUrl;
            String url = gateway + "/customers/" + id;
            log.info("CustomerClient gateway fallback: trying {} for customer {}", url, id);
            WebClient gw = WebClient.builder().baseUrl(gateway).build();
            return gw.get()
                    .uri("/customers/{id}", id)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .map(node -> node.hasNonNull("email") ? node.get("email").asText(null) : null)
                    .timeout(Duration.ofSeconds(3))
                    .doOnError(inner -> log.warn("CustomerClient gateway fallback failed for {}: {}", url, inner.toString()))
                    .onErrorResume(inner -> Mono.empty());
        } catch (Exception ex) {
            log.warn("Gateway fallback failed while fetching customer {}: {}", id, ex.toString());
        }

        return Mono.empty();
    }
}
