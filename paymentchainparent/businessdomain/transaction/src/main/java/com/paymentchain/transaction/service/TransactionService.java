/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.paymentchain.transaction.service;

import com.paymentchain.transaction.dtos.CreateTransactionRequest;
import com.paymentchain.transaction.dtos.TransactionDetailDTO;
import com.paymentchain.transaction.entities.Account;
import com.paymentchain.transaction.entities.Status;
import com.paymentchain.transaction.entities.Transaction;
import com.paymentchain.transaction.exception.BusinessRuleException;
import com.paymentchain.transaction.repository.AccountRepository;
import com.paymentchain.transaction.repository.TransactionRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 *
 * @author casto
 */
@Service
public class TransactionService {
    @Autowired
    TransactionRepository transactionRepository;
    
    @Autowired
    AccountRepository accountRepo;
    
    // Representa el 0.98%
    private static final BigDecimal COMMISSION_RATE = new BigDecimal("0.0098");
    
    @Transactional
    public TransactionDetailDTO performTransaction(CreateTransactionRequest input) throws BusinessRuleException{
        
        if ( (input.getAccountIban().isBlank())){
            BusinessRuleException businessRuleException = new BusinessRuleException("1010", "Error validacion, iban de transacción es obligatorio", HttpStatus.PRECONDITION_FAILED);
                   throw businessRuleException;
        }
        
        Account account = accountRepo.findByIban(input.getAccountIban())
                .orElseThrow(() -> new EntityNotFoundException("Cuenta con IBAN " + input.getAccountIban() + " no existe"));
        
        BigDecimal transactionAmount = input.getAmount(); // Ej: -100.00
        BigDecimal fee = BigDecimal.ZERO;
        BigDecimal totalToDebit;
        
        if (transactionAmount.compareTo(BigDecimal.ZERO) < 0) {

            // A. Calculamos el 0.98% del valor ABSOLUTO
            // abs() convierte -100 a 100 para multiplicar
            BigDecimal rawFee = transactionAmount.abs().multiply(COMMISSION_RATE);

            // B. Redondeo BANCARIO (Importante: 2 decimales, RoundingMode.HALF_EVEN)
            fee = rawFee.setScale(2, RoundingMode.HALF_EVEN);

            // C. El impacto total es el monto (negativo) MENOS el fee (positivo)
            // Ej: -100 - 0.98 = -100.98
            totalToDebit = transactionAmount.subtract(fee);

        } else {
            // Si es depósito (positivo), no hay fee (o lógica distinta)
            totalToDebit = transactionAmount;
        }

        // 3. Validación de Saldo (Business Exception)
        // Calculamos el saldo hipotético futuro
        BigDecimal futureBalance = account.getBalance().add(totalToDebit);

        if (futureBalance.compareTo(BigDecimal.ZERO) < 0) {
            // Lanzamos la excepción con detalles claros
            throw new BusinessRuleException("500","Saldo insuficiente. Saldo actual:"+ account.getBalance()+", Monto + Comisión: "+ totalToDebit.abs(), HttpStatus.PRECONDITION_REQUIRED);
        }

        // 4. Actualizar Estado (Si pasó la validación)
        account.setBalance(futureBalance);
        accountRepo.save(account);
       
        // 5. Guardar Transacción
        Transaction tx = new Transaction();
        tx.setAccount(account);
        tx.setAmount(transactionAmount); // Guardamos el monto solicitado original (-100)
        tx.setFee(fee);                  // Guardamos la comisión aparte (0.98)
        tx.setReference(input.getReference());
        tx.setDescription(input.getDescription());
        tx.setDate(LocalDateTime.now());
        tx.setStatus(Status.LIQUIDADA);
        
        Transaction savedTx = transactionRepository.save(tx);
        
        return mapToDTO(savedTx);
    }
    
    public List<TransactionDetailDTO> findTransactionsByCustomerId (Long customerId){
        List<Transaction> customerTransactions = transactionRepository.findByAccount_CustomerId(customerId);
        return customerTransactions.stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }
    
    public Account createAccount(Account account) {
        // FORZAR QUE SEA NUEVO
        account.setId(null); 

        account.setVersion(null); 

        // Inicializar saldo en cero si viene nulo
        if (account.getBalance() == null) {
            account.setBalance(BigDecimal.ZERO);
        }

        return accountRepo.save(account);
    }
    
    public TransactionDetailDTO mapToDTO(Transaction t) {
        return TransactionDetailDTO.builder()
                .id(t.getId())
                .reference(t.getReference())
                .accountIban(t.getAccount().getIban()) 
                .date(t.getDate())
                .amount(t.getAmount())
                .fee(t.getFee())
                .description(t.getDescription())
                .status(t.getStatus())
                .channel(t.getChannel())
                .build();
    }
}
