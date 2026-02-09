package com.paymentchain.transaction.service;

import com.paymentchain.transaction.entities.IdempotencyKey;
import com.paymentchain.transaction.repository.IdempotencyRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class IdempotencyService {

    private final IdempotencyRepository repository;
    private final StringRedisTemplate redisTemplate;

    private static final String STATUS_LOCKED = "LOCKED";
    private static final String STATUS_COMPLETED = "COMPLETED";
    private static final long REDIS_TTL_MINUTES = 60;


    public boolean lock(String key) {
        // SETNX (Set if Not Exists) - Operación atómica de Redis
        Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(key, STATUS_LOCKED, Duration.ofMinutes(REDIS_TTL_MINUTES));

        return Boolean.TRUE.equals(success);
    }

    /**
     * Busca si ya tenemos una respuesta guardada para esta llave.
     * Primero busca en Redis, si no está, busca en DB.
     */
    public Optional<String> getResponse(String key) {
        String redisVal = redisTemplate.opsForValue().get(key);

        // Si en Redis dice "LOCKED", significa que se está procesando ahora mismo.
        // No devolvemos nada para que el controlador sepa que hay un conflicto.
        if (STATUS_LOCKED.equals(redisVal)) {
            return Optional.empty();
        }

        if (redisVal != null) {
            log.info("Cache Hit! Idempotency Key encontrada en Redis: {}", key);
            return Optional.of(redisVal);
        }

        // 2. Si no estaba en Redis, buscamos en DB
        return repository.findById(key)
                .map(IdempotencyKey::getResponseJson);
    }

    /**
     * Guarda la respuesta final exitosa tanto en DB como en Redis.
     */
    @Transactional
    public void saveSuccess(String key, String responseJson) {
        // 1. Guardar en Base de Datos (Persistencia a largo plazo)
        IdempotencyKey entity = IdempotencyKey.builder()
                .key(key)
                .responseJson(responseJson)
                .status(STATUS_COMPLETED)
                .createdAt(LocalDateTime.now())
                .build();
        repository.save(entity);

        // 2. Actualizar Redis (Para lecturas rápidas siguientes)
        redisTemplate.opsForValue().set(key, responseJson, Duration.ofMinutes(REDIS_TTL_MINUTES));

        log.info("Idempotencia guardada exitosamente para key: {}", key);
    }

    /**
     * Si algo falla, liberamos el candado en Redis para permitir reintentos.
     */
    public void releaseLock(String key) {
        redisTemplate.delete(key);
        log.warn("Lock liberado por fallo para key: {}", key);
    }
}
