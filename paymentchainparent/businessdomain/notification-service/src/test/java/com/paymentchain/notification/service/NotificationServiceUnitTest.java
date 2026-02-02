package com.paymentchain.notification.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paymentchain.notification.client.CustomerClient;
import com.paymentchain.notification.dtos.TransactionResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceUnitTest {

    @InjectMocks
    private NotificationService notificationService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private EmailService emailService;

    @Mock
    private CustomerClient customerClient;

    private TransactionResponse tx;

    @BeforeEach
    void setUp() {
        tx = new TransactionResponse();
        tx.setTransactionId(1L);
        tx.setReference("REF_UNIT_NOTIFY");
        tx.setAmount(new BigDecimal("100"));
        tx.setDate(LocalDateTime.now());
        tx.setCustomerId(10L);
    }

    @Test
    void handleNotification_shouldCallEmail_whenCustomerEmailFound() throws Exception {
        String json = "{}";
        when(objectMapper.readValue(json, TransactionResponse.class)).thenReturn(tx);
        when(customerClient.getEmailById(10L)).thenReturn(Mono.just("user@example.com"));

        notificationService.handleNotification(json);

        verify(emailService).sendTransactionNotification(eq(tx), eq("user@example.com"));
    }

    @Test
    void handleNotification_shouldUseDefaultRecipient_whenCustomerClientFails() throws Exception {
        String json = "{}";
        when(objectMapper.readValue(json, TransactionResponse.class)).thenReturn(tx);
        when(customerClient.getEmailById(10L)).thenReturn(Mono.error(new RuntimeException("down")));

        notificationService.handleNotification(json);

        // if customer lookup fails, emailService should still be called with default recipient (non-null)
        verify(emailService).sendTransactionNotification(eq(tx), any(String.class));
    }

    @Test
    void handleNotification_shouldIgnoreMalformedJson() throws Exception {
        String bad = "not-json";
        when(objectMapper.readValue(bad, TransactionResponse.class)).thenThrow(new RuntimeException("parse"));

        // should not throw
        notificationService.handleNotification(bad);

        verifyNoInteractions(emailService);
    }
}
