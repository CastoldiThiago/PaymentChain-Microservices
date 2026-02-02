package com.paymentchain.transaction.service;

import com.paymentchain.transaction.dtos.CreateAccountRequest;
import com.paymentchain.transaction.entities.Account;
import com.paymentchain.transaction.entities.AccountProduct;
import com.paymentchain.transaction.exception.DuplicateResourceException;
import com.paymentchain.transaction.repository.AccountProductRepository;
import com.paymentchain.transaction.repository.AccountRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountProductRepository productRepository;


    public Page<Account> findAll(Pageable pageable) {
        return accountRepository.findAll(pageable);
    }

    public Account findByIban(String iban) {
        return accountRepository.findByIban(iban).orElse(null);
    }

    public List<Account> findByCustomerId(Long customerId) {
        return accountRepository.findByCustomerId(customerId);
    }


    @Transactional
    public Account create(CreateAccountRequest request) {
        AccountProduct product = productRepository.findById(request.getProductId()).orElse(null);
        if (product == null) throw new RuntimeException("Product not found");

        if (request.getIban() != null && accountRepository.existsByIban(request.getIban())) {
            throw new DuplicateResourceException("iban", "IBAN already exists");
        }

        Account account = new Account();
        account.setIban(request.getIban());
        account.setBalance(request.getBalance());
        account.setCustomerId(request.getCustomerId());
        account.setProduct(product);
        account.setCurrency(request.getCurrency());

        return accountRepository.save(account);
    }

    @Transactional
    public Account update(String iban, CreateAccountRequest request) {
        Account existing = accountRepository.findByIban(iban).orElseThrow(() -> new RuntimeException("Account not found"));
        existing.setBalance(request.getBalance());
        existing.setCurrency(request.getCurrency());
        return accountRepository.save(existing);
    }

    public void delete(String iban) {
        Account existing = accountRepository.findByIban(iban).orElseThrow(() -> new RuntimeException("Account not found"));
        accountRepository.delete(existing);
    }
}
