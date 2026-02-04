package com.paymentchain.notification.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    // Default to API Gateway which routes to customer service; allows us to avoid direct container DNS issues
    @Value("${customer.service.url:http://api-gateway:8080}")
    private String customerServiceUrl;

    @Bean(name = "customerWebClient")
    public WebClient customerWebClient() {
        return WebClient.builder()
                .baseUrl(customerServiceUrl)
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
                        .build())
                .build();
    }
}
