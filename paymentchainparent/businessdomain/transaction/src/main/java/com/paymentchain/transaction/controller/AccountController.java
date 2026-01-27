/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.paymentchain.transaction.controller;

import com.paymentchain.transaction.dtos.AccountResponse;
import com.paymentchain.transaction.dtos.CreateAccountRequest;
import com.paymentchain.transaction.entities.Account;
import com.paymentchain.transaction.entities.AccountProduct;
import com.paymentchain.transaction.mapper.AccountMapper;
import com.paymentchain.transaction.repository.AccountProductRepository;
import com.paymentchain.transaction.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 *
 * @author casto
 */
@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountRepository accountRepository;
    private final AccountProductRepository productRepository;
    private final AccountMapper accountMapper;

    // GET /accounts/{iban} - Consultar saldo y producto
    @GetMapping("/{iban}")
    public ResponseEntity<AccountResponse> getByIban(@PathVariable(name="iban") String iban) {
        return accountRepository.findByIban(iban)
                .map(accountMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /accounts - Crear una cuenta nueva
    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreateAccountRequest request) { // Asumo que ya usas el DTO de entrada que hablamos antes

        AccountProduct product = productRepository.findById(request.getProductId()).orElse(null);

        if (product == null) {
            return ResponseEntity.badRequest().body("Producto no encontrado");
        }

        Account account = new Account();
        account.setIban(request.getIban());
        account.setBalance(request.getBalance());
        account.setCustomerId(request.getCustomerId());
        account.setProduct(product);

        Account saved = accountRepository.save(account);

        // Devolvemos el DTO de respuesta también al crear
        return ResponseEntity.status(HttpStatus.CREATED).body(accountMapper.toResponse(saved));
    }
}
