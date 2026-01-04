/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Repository.java to edit this template
 */
package com.paymentchain.transaction.repository;

import com.paymentchain.transaction.entities.Transaction;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 *
 * @author casto
 */
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
       
    @Query("SELECT t FROM Transaction t WHERE t.accountIban = ?1")
    public List<Transaction> findByAccount(String accountIban);
} 
