package com.paymentchain.apigateway.controller;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v3/api-docs")
public class ApiDocsProxyController {

    private final DiscoveryClient discoveryClient;
    private final WebClient webClient;

    // simple alias map so swagger-ui can use short names
    private static final Map<String, String> ALIAS_MAP = Map.of(
            "transaction", "businessdomain-transaction",
            "customer", "businessdomain-customer"
    );

    public ApiDocsProxyController(DiscoveryClient discoveryClient, WebClient.Builder webClientBuilder) {
        this.discoveryClient = discoveryClient;
        this.webClient = webClientBuilder.build();
    }

    @GetMapping(value = "{serviceId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<String>> docsRoot(@PathVariable("serviceId") String serviceId) {
        String resolved = resolveServiceId(serviceId);
        return proxyToService(resolved, "/v3/api-docs");
    }

    @GetMapping(value = "{serviceId}/**", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<String>> docsWithPath(@PathVariable("serviceId") String serviceId, org.springframework.web.server.ServerWebExchange exchange) {
        String resolved = resolveServiceId(serviceId);
        // Extract the rest of the path after /{serviceId}
        String path = exchange.getRequest().getURI().getPath();
        String prefix = "/v3/api-docs/" + serviceId;
        String suffix = path.length() > prefix.length() ? path.substring(prefix.length()) : "";
        String target = "/v3/api-docs" + suffix;
        return proxyToService(resolved, target);
    }

    private String resolveServiceId(String aliasOrId) {
        if (aliasOrId == null) return null;
        String lower = aliasOrId.toLowerCase();
        return ALIAS_MAP.getOrDefault(lower, aliasOrId);
    }

    private Mono<ResponseEntity<String>> proxyToService(String serviceId, String targetPath) {
        List<ServiceInstance> instances = discoveryClient.getInstances(serviceId);
        if (instances == null || instances.isEmpty()) {
            // Return a minimal OpenAPI skeleton so Swagger UI won't hang
            String skeleton = "{\"openapi\": \"3.0.1\", \"info\": {\"title\": \"" + serviceId + "\", \"version\": \"0.0.0\"}, \"paths\": {}}";
            return Mono.just(ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(skeleton));
        }
        // pick first instance
        ServiceInstance instance = instances.get(0);
        String url = instance.isSecure() ? "https://" : "http://";
        url += instance.getHost() + ":" + instance.getPort() + targetPath;

        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .map(body -> ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(body))
                .onErrorResume(ex -> {
                    String skeleton = "{\"openapi\": \"3.0.1\", \"info\": {\"title\": \"" + serviceId + "\", \"version\": \"0.0.0\"}, \"paths\": {}}";
                    return Mono.just(ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(skeleton));
                });
    }
}
