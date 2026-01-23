/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.paymentchain.transaction.entities;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;
/**
 *
 * @author casto
 */
@Data
@Entity
public class Transaction {
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    private long id;
    private String reference;
    
    @ManyToOne(fetch = FetchType.LAZY) // Lazy para rendimiento
    @JoinColumn(name = "account_id", nullable = false) // Esto crea la Foreign Key en la DB
    private Account account;
    
    private LocalDateTime date;
    private BigDecimal amount;
    private BigDecimal fee;
    private String description;
    
    @Enumerated(EnumType.STRING)
    private Status status;
    @Enumerated(EnumType.STRING)
    private Channel channel;
    
    
}
