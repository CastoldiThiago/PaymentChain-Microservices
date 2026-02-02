package com.paymentchain.transaction;

import com.paymentchain.transaction.client.CurrencyExchangeClient;
import com.paymentchain.transaction.dtos.CreateTransactionRequest;
import com.paymentchain.transaction.dtos.TransactionResponse;
import com.paymentchain.transaction.dtos.TransferRequest;
import com.paymentchain.transaction.entities.Account;
import com.paymentchain.transaction.entities.Transaction;
import com.paymentchain.transaction.enums.TransactionType;
import com.paymentchain.transaction.exception.BusinessRuleException;
import com.paymentchain.transaction.mapper.TransactionMapper;
import com.paymentchain.transaction.repository.AccountRepository;
import com.paymentchain.transaction.repository.TransactionRepository;
import com.paymentchain.transaction.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceUnitTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionMapper mapper;

    @Mock
    private CurrencyExchangeClient currencyExchangeClient;

    @Mock
    private KafkaTemplate<String, TransactionResponse> kafkaTemplate;

    @InjectMocks
    private TransactionService transactionService;

    private Account account;

    @BeforeEach
    void setup() {
        account = new Account();
        account.setIban("AR-UNIT-001");
        account.setBalance(new BigDecimal("1000"));
        account.setCurrency("ARS");
    }

    @Test
    void performDeposit_shouldSaveTransactionAndSendKafka() throws BusinessRuleException {
        CreateTransactionRequest req = new CreateTransactionRequest();
        req.setAccountIban(account.getIban());
        req.setAmount(new BigDecimal("100"));
        req.setCurrency("ARS");
        req.setReference("REF_UNIT_1");
        req.setType(TransactionType.DEPOSIT);

        when(accountRepository.findByIbanForUpdate(account.getIban())).thenReturn(Optional.of(account));

        Transaction saved = new Transaction();
        saved.setId(1L);
        saved.setAccount(account);
        saved.setAmount(new BigDecimal("100"));
        saved.setDate(LocalDateTime.now());

        when(transactionRepository.save(any(Transaction.class))).thenReturn(saved);
        when(mapper.toResponse(any(Transaction.class))).thenReturn(new TransactionResponse());

        TransactionResponse resp = transactionService.performTransaction(req);

        verify(transactionRepository).save(any(Transaction.class));
        verify(kafkaTemplate).send(anyString(), any(TransactionResponse.class));
        assertThat(resp).isNotNull();
    }

    @Test
    void transfer_shouldThrowWhenInsufficientBalance() {
        TransferRequest req = new TransferRequest();
        req.setSourceIban("SRC");
        req.setTargetIban("TGT");
        req.setAmount(new BigDecimal("2000"));

        Account src = new Account();
        src.setIban("SRC");
        src.setBalance(new BigDecimal("100"));
        src.setCurrency("ARS");

        when(accountRepository.findByIbanForUpdate("SRC")).thenReturn(Optional.of(src));
        when(accountRepository.findByIbanForUpdate("TGT")).thenReturn(Optional.of(new Account()));

        assertThrows(BusinessRuleException.class, () -> transactionService.transfer(req));
    }

}
