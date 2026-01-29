/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.paymentchain.transaction.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 *
 * @author casto
 */
@Entity
@Data
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String iban; // Clave de negocio (AR0000...)

    private BigDecimal balance;

    private Long customerId; // ID del cliente

    // (EAGER para tener la regla siempre a mano)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id")
    private AccountProduct product;

    // Relación para ver historial desde la cuenta
    @OneToMany(mappedBy = "account", fetch = FetchType.LAZY)
    private List<Transaction> transactions;

    private String currency;
}
