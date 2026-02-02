package com.paymentchain.transaction;

import com.paymentchain.transaction.client.CurrencyExchangeClient;
import com.paymentchain.transaction.dtos.CreateTransactionRequest;
import com.paymentchain.transaction.dtos.TransactionResponse;
import com.paymentchain.transaction.dtos.TransferRequest;
import com.paymentchain.transaction.entities.Account;
import com.paymentchain.transaction.entities.AccountProduct;
import com.paymentchain.transaction.entities.Transaction;
import com.paymentchain.transaction.enums.TransactionStatus;
import com.paymentchain.transaction.enums.TransactionType;
import com.paymentchain.transaction.exception.BusinessRuleException;
import com.paymentchain.transaction.repository.AccountProductRepository;
import com.paymentchain.transaction.repository.AccountRepository;
import com.paymentchain.transaction.repository.TransactionRepository;
import com.paymentchain.transaction.service.TransactionService;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class TransactionServiceTest extends AbstractIntegrationTest {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountProductRepository accountProductRepository;

    @MockitoBean
    private CurrencyExchangeClient currencyExchangeClient;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
        accountRepository.deleteAll();
        accountProductRepository.deleteAll();

        // Configuración del Mock: Siempre devuelve 1 a 1 en el cambio
        when(currencyExchangeClient.getExchangeRate(anyString(), anyString()))
                .thenReturn(new BigDecimal("0.9"));
    }

    @Test
    @DisplayName("Should perform a DEPOSIT transaction, save to DB and send Kafka event")
    void testPerformTransaction_Deposit() throws BusinessRuleException {
        // -------------------------------------------------
        // 1. GIVEN
        // -------------------------------------------------
        String iban = "AR-DEP-001";
        createAndSaveAccount(iban, new BigDecimal("1000.00"), "ARS", new BigDecimal("0.01"));

        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setAccountIban(iban);
        request.setAmount(new BigDecimal("500.00"));
        request.setCurrency("ARS");
        request.setReference("REF_TEST_01");
        request.setType(TransactionType.DEPOSIT);

        // -------------------------------------------------
        // 2. WHEN
        // -------------------------------------------------
        TransactionResponse response = transactionService.performTransaction(request);

        // -------------------------------------------------
        // 3. THEN
        // -------------------------------------------------
        assertNotNull(response.getTransactionId());

        // A. Verificar Saldo: 1000 + 500 = 1500 (Fee es 0 en depósitos)
        Account updatedAccount = accountRepository.findByIban(iban).orElseThrow();
        assertEquals(0, new BigDecimal("1500.00").compareTo(updatedAccount.getBalance()));

        // B. Verificar Transacción guardada
        List<Transaction> transactions = transactionRepository.findByAccountIban(iban);
        assertEquals(1, transactions.size());
        Transaction savedTx = transactions.getFirst();
        assertEquals(TransactionStatus.COMPLETED, savedTx.getStatus());
        assertEquals(0,new BigDecimal("500.00").compareTo(savedTx.getAmount()));
        assertEquals(0, savedTx.getFee().compareTo(BigDecimal.ZERO)); // Fee debe ser 0

        // -------------------------------------------------
        // 4. THEN (Verificación KAFKA)
        // -------------------------------------------------
        verifyKafkaMessageSent("transaction-topic", savedTx);
    }

    @Test
    @DisplayName("Should transfer funds between two accounts with different currencies applying exchange rate and fees")
    void testTransfer_TransferWithCurrencyExchangeAndFees() throws BusinessRuleException {

        // -------------------------------------------------
        // 1. GIVEN
        // -------------------------------------------------
        String sourceIban = "US-TRF-001";
        String targetIban = "EU-TRF-002";

        createAndSaveAccount(sourceIban, new BigDecimal("2000.00"), "USD", new BigDecimal("0.02"));

        createAndSaveAccount(targetIban, new BigDecimal("500.00"), "EUR", new BigDecimal("0.01"));


        TransferRequest request = new TransferRequest();
        request.setSourceIban(sourceIban);
        request.setTargetIban(targetIban);
        request.setAmount(new BigDecimal("1000.00")); // 1000 USD transfer
        request.setReference("REF_TEST_02");

        // -------------------------------------------------
        // 2. WHEN
        // -------------------------------------------------
        transactionService.transfer(request);

        // -------------------------------------------------
        // 3. THEN
        // -------------------------------------------------

        // A. Verificar Saldos:
        // Cuenta Origen: 2000 - 1000 - 20 (2% fee) = 980
        Account sourceAccount = accountRepository.findByIban(sourceIban).orElseThrow();
        assertEquals(0,new BigDecimal("980.00").compareTo(sourceAccount.getBalance()));

        // Cuenta Destino: 500 + (1000 * 0.9) = 1400
        Account targetAccount = accountRepository.findByIban(targetIban).orElseThrow();
        assertEquals(0, new BigDecimal("1400.00").compareTo(targetAccount.getBalance()));

        // B. Verificar Transacción guardada
        List<Transaction> transactions = transactionRepository.findByAccountIban(sourceIban);
        assertEquals(1, transactions.size());
        Transaction savedTx = transactions.getFirst();
        assertEquals(TransactionStatus.COMPLETED, savedTx.getStatus());


    }

    // --- HELPER METHODS ---

    private void createAndSaveAccount(String iban, BigDecimal balance, String currency, BigDecimal feePercentage) {

        AccountProduct product = new AccountProduct();
        product.setName("Cuenta Standard Test");
        product.setTransactionFeePercentage(feePercentage);
        product = accountProductRepository.save(product);

        Account account = new Account();
        account.setIban(iban);
        account.setBalance(balance);
        account.setCurrency(currency);
        account.setProduct(product);
        accountRepository.save(account);
    }

    private void verifyKafkaMessageSent(String topic, Transaction savedTransaction) {
        // Configuración del consumidor de prueba
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps(kafka.getBootstrapServers(), "test-verify-group", "false");
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        // Configurar Deserializador JSON para leer el objeto TransactionResponse
        JsonDeserializer<TransactionResponse> valueDeserializer = new JsonDeserializer<>(TransactionResponse.class);
        valueDeserializer.addTrustedPackages("*"); // Confiar en todos los paquetes

        Consumer<String, TransactionResponse> consumer = new DefaultKafkaConsumerFactory<>(
                consumerProps,
                new StringDeserializer(),
                valueDeserializer
        ).createConsumer();

        consumer.subscribe(Collections.singletonList(topic));

        // Esperar máximo 5 segundos por el mensaje
        ConsumerRecords<String, TransactionResponse> records = KafkaTestUtils.getRecords(consumer, Duration.ofSeconds(5));


        assertFalse(records.isEmpty(), "Kafka doesn't contain any messages in topic: " + topic);
        TransactionResponse msg = records.iterator().next().value();

        assertEquals(savedTransaction.getAmount(), msg.getAmount(), "Amount in Kafka message does not match the saved transaction.");
        assertEquals(savedTransaction.getReference(), msg.getReference(), "Reference in Kafka message does not match the saved transaction.");

        consumer.close();
    }
}
