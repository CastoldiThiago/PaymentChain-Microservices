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
import com.paymentchain.transaction.exception.BusinessRuleException;
import com.paymentchain.transaction.mapper.TransactionMapper;
import com.paymentchain.transaction.repository.AccountRepository;
import com.paymentchain.transaction.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 *
 * @author casto
 */
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionMapper mapper;

    // 1. INYECTAMOS EL CLIENTE PARA COMUNICARNOS CON EL OTRO SERVICIO
    private final CurrencyExchangeClient currencyExchangeClient;

    @Transactional
    public TransactionResponse performTransaction(CreateTransactionRequest request) throws BusinessRuleException {
        // Bloqueo pesimista para evitar concurrencia
        Account account = accountRepository.findByIbanForUpdate(request.getAccountIban())
                .orElseThrow(() -> new BusinessRuleException("1001", "Cuenta no encontrada", HttpStatus.PRECONDITION_FAILED));

        BigDecimal amount = request.getAmount();
        String requestCurrency = request.getCurrency(); // Ej: "USD"
        String accountCurrency = account.getCurrency(); // Ej: "ARS"

        // 1. Conversión de Moneda (Si aplica)
        if (!requestCurrency.equalsIgnoreCase(accountCurrency)) {
            BigDecimal rate = currencyExchangeClient.getExchangeRate(requestCurrency, accountCurrency);
            amount = amount.multiply(rate);
        }

        // 2. Cálculo de saldo y comisiones (sobre el monto ya convertido o base según regla de negocio)
        // Nota: Simplificamos asumiendo que el fee se calcula sobre el monto final impactado.
        BigDecimal fee = BigDecimal.ZERO;
        BigDecimal totalAction = amount;

        if (amount.compareTo(BigDecimal.ZERO) < 0) { // Es un débito/pago
            BigDecimal positiveAmount = amount.abs();
            if (account.getProduct() != null) {
                fee = positiveAmount.multiply(account.getProduct().getTransactionFeePercentage());
            }
            totalAction = amount.subtract(fee);
            BigDecimal totalDebit = positiveAmount.add(fee);

            if (account.getBalance().compareTo(totalDebit) < 0) {
                throw new BusinessRuleException("1003", "Saldo insuficiente", HttpStatus.PRECONDITION_FAILED);
            }
        }

        account.setBalance(account.getBalance().add(totalAction));
        accountRepository.save(account);

        Transaction transaction = new Transaction();
        transaction.setAccount(account);
        transaction.setDate(LocalDateTime.now());
        transaction.setReference(request.getReference());
        transaction.setAmount(amount);
        transaction.setFee(fee);
        transaction.setTotal(totalAction);
        transaction.setStatus("COMPLETED");

        // Guardamos en qué moneda quedó la transacción (la de la cuenta)
        transaction.setCurrency(accountCurrency);

        return mapper.toResponse(transactionRepository.save(transaction));
    }

    @Transactional
    public void transfer(TransferRequest request) throws BusinessRuleException {
        Account source = accountRepository.findByIbanForUpdate(request.getSourceIban())
                .orElseThrow(() -> new BusinessRuleException("1001", "Cuenta origen no existe", HttpStatus.NOT_FOUND));

        Account target = accountRepository.findByIbanForUpdate(request.getTargetIban())
                .orElseThrow(() -> new BusinessRuleException("1001", "Cuenta destino no existe", HttpStatus.NOT_FOUND));

        if (source.getId().equals(target.getId())) {
            throw new BusinessRuleException("1005", "No puedes transferirte a ti mismo", HttpStatus.BAD_REQUEST);
        }

        // --- LÓGICA MULTI-MONEDA ---
        String sourceCurrency = source.getCurrency();
        String targetCurrency = target.getCurrency();

        BigDecimal sourceAmount = request.getAmount(); // Lo que sale
        BigDecimal targetAmount = sourceAmount;        // Lo que entra

        if (!sourceCurrency.equalsIgnoreCase(targetCurrency)) {
            // Buscamos cuánto vale la moneda origen en términos de destino
            BigDecimal rate = currencyExchangeClient.getExchangeRate(sourceCurrency, targetCurrency);
            targetAmount = sourceAmount.multiply(rate);
        }

        // --- COMISIONES Y SALDOS ---
        BigDecimal fee = BigDecimal.ZERO;
        if (source.getProduct() != null) {
            fee = sourceAmount.multiply(source.getProduct().getTransactionFeePercentage());
        }

        BigDecimal totalDebit = sourceAmount.add(fee);

        if (source.getBalance().compareTo(totalDebit) < 0) {
            throw new BusinessRuleException("1003", "Saldo insuficiente en " + sourceCurrency, HttpStatus.PRECONDITION_FAILED);
        }

        // Actualizar saldos
        source.setBalance(source.getBalance().subtract(totalDebit));
        target.setBalance(target.getBalance().add(targetAmount));

        accountRepository.save(source);
        accountRepository.save(target);

        // --- REGISTROS ---

        // Débito (Salida)
        Transaction debitTx = new Transaction();
        debitTx.setAccount(source);
        debitTx.setAmount(sourceAmount.negate());
        debitTx.setFee(fee);
        debitTx.setTotal(totalDebit.negate());
        debitTx.setDate(LocalDateTime.now());
        debitTx.setStatus("COMPLETED");
        debitTx.setReference("TRANSFER SENT: " + request.getReference());
        debitTx.setCurrency(sourceCurrency); // Guardamos "USD"
        transactionRepository.save(debitTx);

        // Crédito (Entrada)
        Transaction creditTx = new Transaction();
        creditTx.setAccount(target);
        creditTx.setAmount(targetAmount);
        creditTx.setFee(BigDecimal.ZERO);
        creditTx.setTotal(targetAmount);
        creditTx.setDate(LocalDateTime.now());
        creditTx.setStatus("COMPLETED");
        creditTx.setReference("TRANSFER RECEIVED: " + request.getReference());
        creditTx.setCurrency(targetCurrency); // Guardamos "ARS"
        transactionRepository.save(creditTx);
    }
}
