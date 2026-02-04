package com.paymentchain.notification.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paymentchain.notification.client.CustomerClient;
import com.paymentchain.notification.dtos.TransactionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    // Inyectamos Jackson
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmailService emailService;

    @Autowired
    private CustomerClient customerClient;

    @KafkaListener(topics = "transaction-topic", groupId = "notification-group")
    public void handleNotification(String message) {

        try {
            TransactionResponse transaction = objectMapper.readValue(message, TransactionResponse.class);

            // Si llegamos aquí, el JSON era válido y coincide con nuestro DTO
            log.info("NOTIFICACIÓN: Procesando transaccion {}. Para notificar. Monto: {}",
                    transaction.getReference(), transaction.getAmount());

            String recipient = "defaultrecipient@gmail.com";
            try {
                Long customerId = transaction.getCustomerId();
                if (customerId != null) {
                    Mono<String> emailMono = customerClient.getEmailById(customerId);
                    // Increase block timeout a bit to tolerate small network hiccups
                    String email = emailMono.block(Duration.ofSeconds(5));
                    if (email != null && !email.isEmpty()) {
                        recipient = email;
                    } else {
                        log.warn("CustomerClient returned no email for customerId={}", customerId);
                    }
                } else {
                    log.warn("Transaction {} has no customerId; using default recipient", transaction.getReference());
                }
            } catch (Exception e) {
                log.warn("No se pudo obtener el email del cliente para la transacción {}: {}", transaction.getReference(), e.getMessage());
            }

            try {
                emailService.sendTransactionNotification(transaction, recipient);
            } catch (Exception e) {
                log.error("Failed to send notification for transaction {}: {}", transaction.getReference(), e.getMessage());
            }

        } catch (Exception e) {
            log.warn("Malformed transaction JSON received in notification-service: {}", e.getMessage());
        }
    }
}
