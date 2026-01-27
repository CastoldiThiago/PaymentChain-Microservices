/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.paymentchain.transaction.service;

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

    @Transactional
    public TransactionResponse performTransaction(CreateTransactionRequest request) throws BusinessRuleException {

        Account account = accountRepository.findByIban(request.getAccountIban())
                .orElseThrow(() -> new BusinessRuleException("1001",
                        "Cuenta no encontrada: " + request.getAccountIban(),
                        HttpStatus.PRECONDITION_FAILED));

        BigDecimal amount = request.getAmount();
        BigDecimal fee = BigDecimal.ZERO;
        BigDecimal totalAction = amount;

        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            BigDecimal positiveAmount = amount.abs();

            if (account.getProduct() != null) {
                fee = positiveAmount.multiply(account.getProduct().getTransactionFeePercentage());
            }

            totalAction = amount.subtract(fee);
            BigDecimal totalDebit = positiveAmount.add(fee);

            if (account.getBalance().compareTo(totalDebit) < 0) {
                throw new BusinessRuleException("1003",
                        "Saldo insuficiente. Actual: " + account.getBalance() + ", Requerido: " + totalDebit,
                        HttpStatus.PRECONDITION_FAILED);
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

        Transaction savedTransaction = transactionRepository.save(transaction);

        return mapper.toResponse(savedTransaction);
    }

    @Transactional
    public void transfer(TransferRequest request) throws BusinessRuleException {
        // 1. Recuperar ambas cuentas
        Account source = accountRepository.findByIbanForUpdate(request.getSourceIban())
                .orElseThrow(() -> new BusinessRuleException("1001", "Cuenta origen no existe", HttpStatus.NOT_FOUND));

        Account target = accountRepository.findByIbanForUpdate(request.getTargetIban())
                .orElseThrow(() -> new BusinessRuleException("1001", "Cuenta destino no existe", HttpStatus.NOT_FOUND));

        // 2. Validar misma moneda o reglas básicas (Opcional)
        if (source.getId().equals(target.getId())) {
            throw new BusinessRuleException("1005", "No puedes transferirte a ti mismo", HttpStatus.BAD_REQUEST);
        }

        // 3. Simular retiro en Origen (Aplica comisión si corresponde)
        // Reutilizamos la lógica de validación de saldo calculando el débito
        BigDecimal amount = request.getAmount();
        BigDecimal fee = BigDecimal.ZERO;

        if (source.getProduct() != null) {
            fee = amount.multiply(source.getProduct().getTransactionFeePercentage());
        }

        BigDecimal totalDebit = amount.add(fee);

        if (source.getBalance().compareTo(totalDebit) < 0) {
            throw new BusinessRuleException("1003", "Saldo insuficiente en origen", HttpStatus.PRECONDITION_FAILED);
        }

        // 4. Ejecutar Movimientos
        source.setBalance(source.getBalance().subtract(totalDebit));
        target.setBalance(target.getBalance().add(amount)); // Al destino le llega el neto, sin fee

        accountRepository.save(source);
        accountRepository.save(target);

        // 5. Generar Historiales (Dos registros: uno para cada uno)

        // Registro para el que envía (Salida)
        Transaction debitTx = new Transaction();
        debitTx.setAccount(source);
        debitTx.setAmount(amount.negate()); // Negativo
        debitTx.setFee(fee);
        debitTx.setTotal(totalDebit.negate());
        debitTx.setDate(LocalDateTime.now());
        debitTx.setReference("TRANSFER SENT: " + request.getReference());
        debitTx.setStatus("COMPLETED");
        transactionRepository.save(debitTx);

        // Registro para el que recibe (Entrada)
        Transaction creditTx = new Transaction();
        creditTx.setAccount(target);
        creditTx.setAmount(amount); // Positivo
        creditTx.setFee(BigDecimal.ZERO); // quien recibe no paga comisión
        creditTx.setTotal(amount);
        creditTx.setDate(LocalDateTime.now());
        creditTx.setReference("TRANSFER RECEIVED: " + request.getReference());
        creditTx.setStatus("COMPLETED");
        transactionRepository.save(creditTx);
    }

}
