package com.paymentchain.currencyexchange.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@Slf4j
public class CurrencyService {

    @Autowired
    private CurrencyCacheService currencyCacheService;

    @Autowired
    private CacheManager cacheManager;

    /**
     * Public method: validates parameters, then delegates to cacheable service.
     */
    public BigDecimal getExchangeRate(String from, String to) {
        log.info("[DEBUG] getExchangeRate called with from='{}', to='{}'", from, to);
        if (from == null || to == null) {
            log.error("[ERROR] CurrencyService.getExchangeRate called with null parameters: from={}, to={}", from, to, new Exception("StackTrace for null currency"));
            throw new IllegalArgumentException("Currency codes 'from' and 'to' must not be null");
        }
        String fromSan = from.trim();
        String toSan = to.trim();
        if (fromSan.isEmpty() || toSan.isEmpty() || "null".equalsIgnoreCase(fromSan) || "null".equalsIgnoreCase(toSan)) {
            log.error("[ERROR] CurrencyService.getExchangeRate called with invalid parameters: from='{}', to='{}'", from, to);
            throw new IllegalArgumentException("Currency codes 'from' and 'to' must be non-empty and not 'null'");
        }

        // Try to read from cache manually first (key format: FROM_TO uppercase)
        String key = fromSan.toUpperCase() + "_" + toSan.toUpperCase();
        try {
            if (cacheManager != null) {
                log.info("[CACHE DEBUG] cacheManager implementation = {}", cacheManager.getClass().getName());
                Cache cache = cacheManager.getCache("exchangeRates");
                log.info("[CACHE DEBUG] cache instance = {}", cache == null ? "null" : cache.getClass().getName());
                if (cache != null) {
                    Cache.ValueWrapper vw = cache.get(key);
                    if (vw != null && vw.get() instanceof BigDecimal) {
                        BigDecimal cached = (BigDecimal) vw.get();
                        log.info("[CACHE HIT] exchangeRates key='{}' value={}", key, cached);
                        return cached;
                    } else if (vw != null) {
                        log.info("[CACHE HIT] exchangeRates key='{}' value present but not BigDecimal: {}", key, vw.get());
                    } else {
                        log.info("[CACHE MISS] exchangeRates key='{}'", key);
                    }
                } else {
                    log.warn("[CACHE] Cache 'exchangeRates' not found in CacheManager");
                }
            }
        } catch (Exception e) {
            log.warn("[CACHE] Error while reading cache for key '{}': {}", key, e.getMessage());
        }

        // Delegate to cacheable method (it will fetch and also write manually as a safeguard)
        return currencyCacheService.getExchangeRateCached(fromSan, toSan);
    }
}