package com.paymentchain.notification.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paymentchain.notification.dtos.TransactionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    // Inyectamos Jackson
    @Autowired
    private ObjectMapper objectMapper;

    @KafkaListener(topics = "transaction-topic", groupId = "notification-group")
    public void handleNotification(String message) { // 👈 Recibimos String, no Objeto

        try {
            // INTENTO MANUAL DE CONVERSIÓN
            TransactionResponse transaction = objectMapper.readValue(message, TransactionResponse.class);

            // Si llegamos aquí, el JSON era válido y coincide con nuestro DTO
            log.info("🔔 NOTIFICACIÓN: Transacción {} procesada. Monto: {}",
                    transaction.getReference(), transaction.getAmount());

            // enviarEmail(transaction)...

        } catch (Exception e) {
            // 🔥 CONTROL DE ERRORES (POISON PILL)
            // Si el mensaje es basura, lo logueamos y LO DESCARTAMOS para no bloquear la cola.
            log.error("❌ Error procesando mensaje de Kafka: {}", message, e);
        }
    }
}
