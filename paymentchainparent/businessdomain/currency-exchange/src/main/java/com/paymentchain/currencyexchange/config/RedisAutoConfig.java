package com.paymentchain.currencyexchange.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Configuration
public class RedisAutoConfig {

    private static final Logger logger = LoggerFactory.getLogger(RedisAutoConfig.class);

    @Value("${spring.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.redis.port:6379}")
    private int redisPort;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        logger.info("Creating LettuceConnectionFactory with host={} port={}", redisHost, redisPort);
        LettuceConnectionFactory factory = new LettuceConnectionFactory(redisHost, redisPort);
        factory.afterPropertiesSet();
        return factory;
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory factory) {
        logger.info("Creating StringRedisTemplate");
        return new StringRedisTemplate(factory);
    }

    @Primary
    @Bean("cacheManager")
    public CacheManager cacheManager(RedisConnectionFactory factory) {
        logger.info("Building primary RedisCacheManager (wrapped with logging)");

        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1))
                .disableCachingNullValues()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));

        RedisCacheManager delegate = RedisCacheManager.builder(factory)
                .cacheDefaults(config)
                .build();
        logger.info("Delegate created with caches: {}", delegate.getCacheNames());
        return new CacheManager() {
            @Override
            public Cache getCache(String name) {
                Cache delegateCache = delegate.getCache(name);
                if (delegateCache == null) return null;
                return new Cache() {
                    @Override
                    public String getName() {
                        return delegateCache.getName();
                    }

                    @Override
                    public Object getNativeCache() {
                        return delegateCache.getNativeCache();
                    }

                    @Override
                    public ValueWrapper get(Object key) {
                        logger.info("[CACHE GET] cache='{}' key='{}'", name, key);
                        ValueWrapper vw = delegateCache.get(key);
                        logger.info("[CACHE GET RESULT] cache='{}' key='{}' valuePresent={}", name, key, vw != null);
                        return vw;
                    }

                    @Override
                    public <T> T get(Object key, Class<T> type) {
                        logger.info("[CACHE GET_TYPED] cache='{}' key='{}' type={}", name, key, type);
                        return delegateCache.get(key, type);
                    }

                    @Override
                    public <T> T get(Object key, java.util.concurrent.Callable<T> valueLoader) {
                        logger.info("[CACHE GET_OR_LOAD] cache='{}' key='{}'", name, key);
                        try {
                            T val = delegateCache.get(key, valueLoader);
                            logger.info("[CACHE LOADED] cache='{}' key='{}'", name, key);
                            return val;
                        } catch (Exception e) {
                            logger.error("Error loading cache key {}: {}", key, e.getMessage());
                            throw new RuntimeException(e);
                        }
                    }

                    @Override
                    public void put(Object key, Object value) {
                        logger.info("[CACHE PUT] cache='{}' key='{}' value='{}'", name, key, value);
                        delegateCache.put(key, value);
                    }

                    @Override
                    public ValueWrapper putIfAbsent(Object key, Object value) {
                        logger.info("[CACHE PUT_IF_ABSENT] cache='{}' key='{}' value='{}'", name, key, value);
                        return delegateCache.putIfAbsent(key, value);
                    }

                    @Override
                    public void evict(Object key) {
                        logger.info("[CACHE EVICT] cache='{}' key='{}'", name, key);
                        delegateCache.evict(key);
                    }

                    @Override
                    public void clear() {
                        logger.info("[CACHE CLEAR] cache='{}'", name);
                        delegateCache.clear();
                    }
                };
            }

            @Override
            public Collection<String> getCacheNames() {
                Set<String> names = new HashSet<>(delegate.getCacheNames());
                return names;
            }
        };
    }
}
