package com.paymentchain.transaction.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "idempotency_keys")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IdempotencyKey {

    @Id
    @Column(name = "key_id", length = 100)
    private String key; // El UUID que manda el frontend

    @Column(name = "response_json", columnDefinition = "TEXT")
    private String responseJson; // Guardamos la respuesta exacta que dimos

    @Column(name = "status")
    private String status; // "LOCKED", "COMPLETED", "FAILED"

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
