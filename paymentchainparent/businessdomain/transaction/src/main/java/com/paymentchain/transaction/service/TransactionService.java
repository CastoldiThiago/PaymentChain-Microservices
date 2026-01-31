/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.paymentchain.transaction.service;

import com.paymentchain.transaction.client.CurrencyExchangeClient;
import com.paymentchain.transaction.dtos.CreateTransactionRequest;
import com.paymentchain.transaction.dtos.TransactionResponse;
import com.paymentchain.transaction.dtos.TransferRequest;
import com.paymentchain.transaction.entities.Account;
import com.paymentchain.transaction.entities.Transaction;
import com.paymentchain.transaction.enums.TransactionStatus;
import com.paymentchain.transaction.exception.BusinessRuleException;
import com.paymentchain.transaction.mapper.TransactionMapper;
import com.paymentchain.transaction.repository.AccountRepository;
import com.paymentchain.transaction.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Transaction domain logic
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionMapper mapper;
    private final CurrencyExchangeClient currencyExchangeClient;

    private final KafkaTemplate<String, TransactionResponse> kafkaTemplate;

    @Transactional
    public TransactionResponse performTransaction(CreateTransactionRequest request) throws BusinessRuleException {
        Account account = accountRepository.findByIbanForUpdate(request.getAccountIban())
                .orElseThrow(() -> new BusinessRuleException("1001", "Cuenta no encontrada", HttpStatus.PRECONDITION_FAILED));

        BigDecimal amount = request.getAmount();
        if (!request.getCurrency().equalsIgnoreCase(account.getCurrency())) {
            BigDecimal rate = currencyExchangeClient.getExchangeRate(request.getCurrency(), account.getCurrency());
            amount = amount.multiply(rate);
        }


        BigDecimal fee = calculateFee(amount, account);
        BigDecimal totalAction = getTotalAction(amount, fee, account);

        account.setBalance(account.getBalance().add(totalAction));
        accountRepository.save(account);

        // Construir Transacción
        Transaction transaction = new Transaction();
        transaction.setAccount(account);
        transaction.setReference(request.getReference());
        transaction.setAmount(amount);
        transaction.setFee(fee);
        transaction.setTotal(totalAction);
        transaction.setDate(LocalDateTime.now());
        transaction.setStatus(TransactionStatus.COMPLETED.name()); // ✅ Usando Enum
        transaction.setCurrency(account.getCurrency());

        // Guardar
        Transaction savedTransaction = transactionRepository.save(transaction);

        // 🔥 2. DISPARAR EVENTO KAFKA (Solo si se guardó bien)
        // Esto notifica al NotificationService
        this.sendNotification(mapper.toResponse(savedTransaction));

        return mapper.toResponse(savedTransaction);
    }

    @Transactional
    public void transfer(TransferRequest request) throws BusinessRuleException {
        // ... (Tu lógica de búsqueda y validación de cuentas igual) ...
        Account source = accountRepository.findByIbanForUpdate(request.getSourceIban())
                .orElseThrow(() -> new BusinessRuleException("1001", "Cuenta origen no existe", HttpStatus.NOT_FOUND));
        Account target = accountRepository.findByIbanForUpdate(request.getTargetIban())
                .orElseThrow(() -> new BusinessRuleException("1001", "Cuenta destino no existe", HttpStatus.NOT_FOUND));

        // ... (Lógica de conversión multi-moneda igual) ...

        // --- PROCESAR DÉBITO (ORIGEN) ---
        BigDecimal sourceAmount = request.getAmount();
        BigDecimal fee = calculateFee(sourceAmount, source); // ♻️ Reusando método
        BigDecimal totalDebit = sourceAmount.add(fee);

        if (source.getBalance().compareTo(totalDebit) < 0) {
            throw new BusinessRuleException("1003", "Saldo insuficiente", HttpStatus.PRECONDITION_FAILED);
        }

        source.setBalance(source.getBalance().subtract(totalDebit));

        // --- PROCESAR CRÉDITO (DESTINO) ---
        // (Asumimos conversión de moneda ya hecha en 'targetAmount')
        BigDecimal targetAmount = request.getAmount(); // O el convertido
        target.setBalance(target.getBalance().add(targetAmount));

        accountRepository.save(source);
        accountRepository.save(target);

        // --- REGISTRAR TRANSACCIONES Y NOTIFICAR ---

        // 1. Guardar Débito
        Transaction debitTx = createTransactionEntity(source, sourceAmount.negate(), fee, "TRANSFER SENT: " + request.getReference());
        Transaction savedDebit = transactionRepository.save(debitTx);

        // 🔥 Notificar al que envía el dinero
        this.sendNotification(mapper.toResponse(savedDebit));

        // 2. Guardar Crédito
        Transaction creditTx = createTransactionEntity(target, targetAmount, BigDecimal.ZERO, "TRANSFER RECEIVED: " + request.getReference());
        Transaction savedCredit = transactionRepository.save(creditTx);

        // 🔥 Notificar al que recibe el dinero
        this.sendNotification(mapper.toResponse(savedCredit));
    }

    // Read helpers
    public List<Transaction> findAll() {
        return transactionRepository.findAll();
    }

    public Page<Transaction> findAll(Pageable pageable) {
        return transactionRepository.findAll(pageable);
    }

    public Optional<Transaction> findById(Long id) {
        return transactionRepository.findById(id);
    }

    public List<Transaction> findByAccountIban(String iban) {
        return transactionRepository.findByAccountIban(iban);
    }

    public Page<Transaction> findByAccountIban(String iban, Pageable pageable) {
        return transactionRepository.findByAccountIban(iban, pageable);
    }

    // ---------------------------------------------------
    // MÉTODOS PRIVADOS (HELPER METHODS)
    // ---------------------------------------------------

    private BigDecimal calculateFee(BigDecimal amount, Account account) {
        if (amount.compareTo(BigDecimal.ZERO) > 0) return BigDecimal.ZERO;
        // Lógica de fee simplificada
        if (account.getProduct() != null) {
            return amount.abs().multiply(account.getProduct().getTransactionFeePercentage());
        }
        return BigDecimal.ZERO;
    }

    // Método helper para no repetir el new Transaction()...
    private Transaction createTransactionEntity(Account account, BigDecimal amount, BigDecimal fee, String reference) {
        Transaction tx = new Transaction();
        tx.setAccount(account);
        tx.setAmount(amount);
        tx.setFee(fee);
        tx.setTotal(amount.subtract(fee));
        tx.setDate(LocalDateTime.now());
        tx.setStatus(TransactionStatus.COMPLETED.name());
        tx.setReference(reference);
        tx.setCurrency(account.getCurrency());
        return tx;
    }

    private void sendNotification(TransactionResponse transaction) {
        log.info("🚀 Enviando evento a Kafka para la transacción: {}", transaction.getReference());
        // Enviamos todo el objeto Transaction.
        // Spring Boot lo convertirá a JSON automáticamente gracias a la config que hicimos.
        kafkaTemplate.send("transaction-topic", transaction);
    }

    private static @NonNull BigDecimal getTotalAction(BigDecimal amount, BigDecimal fee, Account account) throws BusinessRuleException {
        BigDecimal totalAction = amount;

        // Validar saldo si es débito
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            BigDecimal totalDebit = amount.abs().add(fee);
            if (account.getBalance().compareTo(totalDebit) < 0) {
                throw new BusinessRuleException("1003", "Saldo insuficiente", HttpStatus.PRECONDITION_FAILED);
            }
            totalAction = amount.subtract(fee); // Si es debito, restamos el fee adicionalmente
        }
        return totalAction;
    }
}
