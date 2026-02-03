package com.paymentchain.currencyexchange.service;

import com.paymentchain.currencyexchange.dtos.ExchangeRateApiResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

@Service
@Slf4j
public class CurrencyService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${external.api.exchange-rate.url}")
    private String baseUrl;

    @Value("${external.api.exchange-rate.key}")
    private String apiKey;

    /**
     * Endpoint Pair Conversion: https://v6.exchangerate-api.com/v6/YOUR-API-KEY/pair/EUR/GBP
     */
    @Cacheable(value = "exchangeRates", key = "#from + '_' + #to")
    @CircuitBreaker(name = "externalApi", fallbackMethod = "fallbackRate")
    public BigDecimal getExchangeRate(String from, String to) {

        String fromUpper = from.toUpperCase();
        String toUpper = to.toUpperCase();

        log.info("⚡ Buscando tasa en ExchangeRate-API: {} -> {}", fromUpper, toUpper);

        if (fromUpper.equals(toUpper)) return BigDecimal.ONE;

        // Construimos la URL dinámica
        // Estructura: BASE_URL / KEY / pair / FROM / TO
        String url = String.format("%s/%s/pair/%s/%s", baseUrl, apiKey, fromUpper, toUpper);

        try {
            ExchangeRateApiResponse response = restTemplate.getForObject(url, ExchangeRateApiResponse.class);

            if (response != null && "success".equalsIgnoreCase(response.getResult()) && response.getConversionRate() != null) {
                return response.getConversionRate();
            } else {
                log.error("Error en respuesta de API: {}", response);
                throw new RuntimeException("Error obteniendo tasa de cambio");
            }

        } catch (Exception e) {
            log.error("Fallo al llamar a ExchangeRate-API: {}", e.getMessage());
            throw e; // Lanza para activar el CircuitBreaker
        }
    }

    // --- FALLBACK METHOD ---
    public BigDecimal fallbackRate(String from, String to, Throwable t) {
        log.warn("🛡️ Activando Fallback para: {} -> {}. Causa: {}", from, to, t.getMessage());

        // Valores 'Hardcodeados' de emergencia por si se te acaba el plan gratuito o cae la API
        if ("USD".equalsIgnoreCase(from) && "ARS".equalsIgnoreCase(to)) return new BigDecimal("1400"); // Dólar Blue aprox
        if ("EUR".equalsIgnoreCase(from) && "ARS".equalsIgnoreCase(to)) return new BigDecimal("1500");

        return BigDecimal.ONE;
    }
}