package com.paymentchain.transaction;

import com.paymentchain.transaction.dtos.TransactionResponse;
import com.paymentchain.transaction.dtos.TransferRequest;
import com.paymentchain.transaction.entities.Account;
import com.paymentchain.transaction.entities.AccountProduct;
import com.paymentchain.transaction.entities.Transaction;
import com.paymentchain.transaction.exception.BusinessRuleException;
import com.paymentchain.transaction.mapper.TransactionMapper;
import com.paymentchain.transaction.repository.AccountRepository;
import com.paymentchain.transaction.repository.TransactionRepository;
import com.paymentchain.transaction.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceKafkaUnitTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionMapper mapper;

    @Mock
    private org.springframework.kafka.core.KafkaTemplate<String, TransactionResponse> kafkaTemplate;

    @Mock
    private com.paymentchain.transaction.client.CurrencyExchangeClient currencyExchangeClient;

    @InjectMocks
    private TransactionService transactionService;

    private Account source;
    private Account target;

    @BeforeEach
    void setUp() {
        AccountProduct product = new AccountProduct();
        product.setTransactionFeePercentage(new BigDecimal("0.02"));

        source = new Account();
        source.setIban("SRC-IBAN");
        source.setBalance(new BigDecimal("2000"));
        source.setCurrency("USD");
        source.setProduct(product);

        target = new Account();
        target.setIban("TGT-IBAN");
        target.setBalance(new BigDecimal("500"));
        target.setCurrency("EUR");
        target.setProduct(product);

        // currency exchange mock: USD -> EUR rate 0.9
        when(currencyExchangeClient.getExchangeRate(any(), any())).thenReturn(new BigDecimal("0.9"));

        // map Transaction -> TransactionResponse by copying reference and amount
        when(mapper.toResponse(any(Transaction.class))).thenAnswer(inv -> {
            Transaction tx = inv.getArgument(0);
            TransactionResponse resp = new TransactionResponse();
            resp.setReference(tx.getReference());
            resp.setAmount(tx.getAmount());
            return resp;
        });
    }

    @Test
    void transfer_shouldSendTwoKafkaMessages() throws BusinessRuleException {
        // Arrange
        when(accountRepository.findByIbanForUpdate("SRC-IBAN")).thenReturn(Optional.of(source));
        when(accountRepository.findByIbanForUpdate("TGT-IBAN")).thenReturn(Optional.of(target));

        // simulate saving debit and credit transactions: just return the same object passed
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        TransferRequest req = new TransferRequest();
        req.setSourceIban("SRC-IBAN");
        req.setTargetIban("TGT-IBAN");
        req.setAmount(new BigDecimal("1000"));
        req.setReference("REF_TEST_KAFKA");

        // Act
        transactionService.transfer(req);

        // Assert: kafkaTemplate.send called twice with TransactionResponse whose reference matches sent/received references
        ArgumentCaptor<TransactionResponse> captor = ArgumentCaptor.forClass(TransactionResponse.class);
        verify(kafkaTemplate, times(2)).send(anyString(), captor.capture());

        assertThat(captor.getAllValues()).hasSize(2);
        boolean foundSent = captor.getAllValues().stream().anyMatch(r -> r.getReference() != null && r.getReference().contains("TRANSFER SENT"));
        boolean foundReceived = captor.getAllValues().stream().anyMatch(r -> r.getReference() != null && r.getReference().contains("TRANSFER RECEIVED"));

        assertThat(foundSent).isTrue();
        assertThat(foundReceived).isTrue();
    }
}
