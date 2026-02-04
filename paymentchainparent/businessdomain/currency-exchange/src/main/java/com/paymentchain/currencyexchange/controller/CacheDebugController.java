package com.paymentchain.currencyexchange.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping("/internal/cache")
@ConditionalOnProperty(prefix = "cache.debug", name = "enabled", havingValue = "true", matchIfMissing = false)
public class CacheDebugController {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @GetMapping("/exchange/keys")
    public ResponseEntity<Set<String>> keys() {
        Set<String> keys = stringRedisTemplate.keys("exchangeRates*");
        return ResponseEntity.ok(keys);
    }

    @GetMapping("/exchange/{key}")
    public ResponseEntity<String> get(@PathVariable("key") String key) {
        String value = stringRedisTemplate.opsForValue().get(key);
        return ResponseEntity.ok(value);
    }
}
