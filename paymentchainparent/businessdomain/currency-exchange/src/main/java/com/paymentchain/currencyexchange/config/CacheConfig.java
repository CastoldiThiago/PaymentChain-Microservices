package com.paymentchain.currencyexchange.config;

import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.cache.interceptor.KeyGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.lang.reflect.Method;

@Configuration
public class CacheConfig {
    private static final Logger logger = LoggerFactory.getLogger(CacheConfig.class);

    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer() {
        return (builder) -> builder
            .withCacheConfiguration("exchangeRates",
                RedisCacheConfiguration.defaultCacheConfig()
                    .entryTtl(Duration.ofHours(1))
                    .disableCachingNullValues()
                    .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
            )
            .withCacheConfiguration("currencyCache",
                RedisCacheConfiguration.defaultCacheConfig()
                    .entryTtl(Duration.ofMinutes(30))
                    .disableCachingNullValues()
                    .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
            );
    }

    @Bean
    public RedisCacheConfiguration redisCacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1))
                .disableCachingNullValues()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));
    }

    @Bean
    public KeyGenerator keyGenerator() {
        return new KeyGenerator() {
            @Override
            public Object generate(Object target, Method method, Object... params) {
                if (params == null || params.length == 0) {
                    throw new IllegalArgumentException("Cache key parameters must not be empty");
                }
                StringBuilder sb = new StringBuilder();
                for (Object p : params) {
                    if (p == null) {
                        logger.error("Attempt to generate cache key with null parameter for {}#{}", target.getClass().getSimpleName(), method.getName());
                        throw new IllegalArgumentException("Cache key parameter is null");
                    }
                    String s = p.toString().trim();
                    if (s.isEmpty() || "null".equalsIgnoreCase(s)) {
                        logger.error("Attempt to generate cache key with invalid parameter ('{}') for {}#{}", p, target.getClass().getSimpleName(), method.getName());
                        throw new IllegalArgumentException("Cache key parameter is invalid: '" + p + "'");
                    }
                    if (sb.length() > 0) sb.append('_');
                    sb.append(s);
                }
                return sb.toString();
            }
        };
    }
}
