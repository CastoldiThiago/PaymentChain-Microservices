/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.paymentchain.transaction.entities;

import com.paymentchain.transaction.enums.TransactionStatus;
import com.paymentchain.transaction.enums.TransactionType;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
/**
 *
 * @author casto
 */
@Entity
@Data
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime date;
    private String reference;

    private BigDecimal amount;
    private BigDecimal fee;
    private BigDecimal total;

    private TransactionStatus status; // PENDING, COMPLETED, REJECTED

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;
    private String currency;
    private TransactionType type; // DEPOSIT, WITHDRAWAL
}
