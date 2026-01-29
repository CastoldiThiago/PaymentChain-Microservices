package com.paymentchain.currencyexchange.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;

@Service
@Slf4j
public class CurrencyService {

    // Usamos RestTemplate para llamar a una API externa real
    private final RestTemplate restTemplate = new RestTemplate();

    // API Gratuita de Tasas de Cambio (No requiere Key)
    private static final String API_URL = "https://api.frankfurter.dev/v1/latest?base=${from}&symbols=${to}";

    /**
     * @Cacheable: Antes de ejecutar este método, Spring revisa en Redis.
     * Si ya existe la clave "USD_ARS", devuelve el valor guardado y NO ejecuta el método.
     * Si no existe, ejecuta el método, llama a la API, y guarda el resultado en Redis.
     */
    @Cacheable(value = "exchangeRates", key = "#from + '_' + #to")
    @CircuitBreaker(name = "externalApi", fallbackMethod = "fallbackRate") // Protección contra fallos
    public BigDecimal getExchangeRate(String from, String to) {

        log.info("⚡ Buscando tasa en API Externa (No estaba en Caché): {} -> {}", from, to);

        // Caso base: misma moneda
        if (from.equalsIgnoreCase(to)) return BigDecimal.ONE;

        // Llamada a API Externa
        try {
            // La API devuelve algo como: {"amount":1.0,"base":"USD","date":"2024-01-29","rates":{"ARS":820.5}}
            Map<String, Object> response = restTemplate.getForObject(API_URL, Map.class, from, to);

            if (response != null && response.containsKey("rates")) {
                Map<String, Double> rates = (Map<String, Double>) response.get("rates");
                if (rates.containsKey(to)) {
                    return BigDecimal.valueOf(rates.get(to));
                }
            }
        } catch (Exception e) {
            log.error("Error llamando a API externa: {}", e.getMessage());
            throw e; // Lanzamos para que Resilience4j lo capture y active el fallback
        }

        return BigDecimal.ONE; // Default si no encuentra nada
    }

    // --- FALLBACK METHOD ---
    // Se ejecuta si la API externa está caída o da timeout.
    public BigDecimal fallbackRate(String from, String to, Throwable t) {
        log.warn("⚠️ API Caída. Usando tasa de contingencia para {} -> {}. Error: {}", from, to, t.getMessage());

        // En un caso real, podrías buscar en una tabla de BD histórica.
        // Para el demo, devolvemos una tasa fija o simulada.
        if ("USD".equalsIgnoreCase(from) && "ARS".equalsIgnoreCase(to)) return new BigDecimal("1000");
        if ("EUR".equalsIgnoreCase(from) && "ARS".equalsIgnoreCase(to)) return new BigDecimal("1100");

        return BigDecimal.ONE;
    }
}