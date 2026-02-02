package com.paymentchain.transaction.service;

import com.paymentchain.transaction.dtos.CreateAccountRequest;
import com.paymentchain.transaction.entities.Account;
import com.paymentchain.transaction.entities.AccountProduct;
import com.paymentchain.transaction.exception.DuplicateResourceException;
import com.paymentchain.transaction.repository.AccountProductRepository;
import com.paymentchain.transaction.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceUnitTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountProductRepository productRepository;

    @InjectMocks
    private AccountService accountService;

    private AccountProduct product;

    @BeforeEach
    void setUp() {
        product = new AccountProduct();
        product.setId(1L);
        product.setName("Standard");
        product.setTransactionFeePercentage(new BigDecimal("0.01"));
    }

    @Test
    void create_shouldThrowWhenProductNotFound() {
        CreateAccountRequest req = new CreateAccountRequest();
        req.setProductId(99L);

        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> accountService.create(req));
        assertThat(ex.getMessage()).contains("Product not found");
    }

    @Test
    void create_shouldThrowWhenIbanExists() {
        CreateAccountRequest req = new CreateAccountRequest();
        req.setProductId(1L);
        req.setIban("EXISTING_IBAN");

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(accountRepository.existsByIban("EXISTING_IBAN")).thenReturn(true);

        DuplicateResourceException ex = assertThrows(DuplicateResourceException.class, () -> accountService.create(req));
        assertThat(ex.getField()).isEqualTo("iban");
    }

    @Test
    void create_shouldSaveAccount() {
        CreateAccountRequest req = new CreateAccountRequest();
        req.setProductId(1L);
        req.setIban("NEW_IBAN");
        req.setBalance(new BigDecimal("1000"));
        req.setCurrency("ARS");
        req.setCustomerId(5L);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(accountRepository.existsByIban("NEW_IBAN")).thenReturn(false);

        Account saved = new Account();
        saved.setIban("NEW_IBAN");
        saved.setBalance(req.getBalance());
        saved.setCurrency(req.getCurrency());
        saved.setCustomerId(req.getCustomerId());

        when(accountRepository.save(any(Account.class))).thenReturn(saved);

        Account result = accountService.create(req);

        assertThat(result.getIban()).isEqualTo("NEW_IBAN");
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void update_shouldThrowWhenNotFound() {
        CreateAccountRequest req = new CreateAccountRequest();
        req.setBalance(new BigDecimal("10"));
        when(accountRepository.findByIban("NO_IBAN")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> accountService.update("NO_IBAN", req));
        assertThat(ex.getMessage()).contains("Account not found");
    }

}
