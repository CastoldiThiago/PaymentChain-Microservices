package com.paymentchain.currencyexchange.controller;

import com.paymentchain.currencyexchange.service.CurrencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/internal/cache")
@ConditionalOnProperty(prefix = "cache.debug", name = "enabled", havingValue = "true", matchIfMissing = false)
public class DebugCacheTestController {

    @Autowired
    private CurrencyService currencyService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> testCache(@RequestParam("from") String from, @RequestParam("to") String to) {
        // First call
        BigDecimal first = currencyService.getExchangeRate(from, to);
        // Second call - should be served from cache
        BigDecimal second = currencyService.getExchangeRate(from, to);

        Set<String> keys = stringRedisTemplate.keys("exchangeRates*");
        Map<String, Object> resp = Map.of(
                "first", first,
                "second", second,
                "redisKeys", keys
        );
        return ResponseEntity.ok(resp);
    }
}
