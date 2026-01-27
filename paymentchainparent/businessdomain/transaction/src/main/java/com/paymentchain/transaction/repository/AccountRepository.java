/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Repository.java to edit this template
 */
package com.paymentchain.transaction.repository;

import com.paymentchain.transaction.entities.Account;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 *
 * @author casto
 */
public interface AccountRepository extends JpaRepository<Account, Long> {

    // 1. Para el Controller (Lectura simple, SIN bloqueo)
    Optional<Account> findByIban(String iban);

    // 2. Para el Service (Escritura segura, CON bloqueo)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.iban = :iban")
    Optional<Account> findByIbanForUpdate(@Param("iban") String iban);

    List<Account> findByCustomerId(Long id);
}
