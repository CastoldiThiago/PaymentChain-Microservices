/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Repository.java to edit this template
 */
package com.paymentchain.transaction.repository;

import com.paymentchain.transaction.entities.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 *
 * @author casto
 */
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByAccountIban(String iban);

    // Pageable overloads
    Page<Transaction> findByAccountIban(String iban, Pageable pageable);

    Page<Transaction> findAll(Pageable pageable);
}
