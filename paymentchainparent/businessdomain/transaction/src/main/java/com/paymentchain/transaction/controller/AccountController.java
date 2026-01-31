/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.paymentchain.transaction.controller;

import com.paymentchain.transaction.dtos.AccountResponse;
import com.paymentchain.transaction.dtos.CreateAccountRequest;
import com.paymentchain.transaction.entities.Account;
import com.paymentchain.transaction.mapper.AccountMapper;
import com.paymentchain.transaction.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 *
 * @author casto
 */
@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
@Tag(name = "Accounts", description = "Account management APIs")
public class AccountController {

    private final AccountService accountService;
    private final AccountMapper accountMapper;

    @Operation(summary = "Get account by IBAN")
    // GET /accounts/{iban} - Consultar saldo y producto
    @GetMapping("/{iban}")
    public ResponseEntity<AccountResponse> getByIban(@PathVariable(name = "iban") String iban) {
        Account account = accountService.findByIban(iban);
        if (account == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(accountMapper.toResponse(account));
    }

    @Operation(summary = "Create an account")
    // POST /accounts - Crear una cuenta nueva
    @PostMapping
    public ResponseEntity<AccountResponse> create(@Valid @RequestBody CreateAccountRequest request) { // Asumo que ya usas el DTO de entrada que hablamos antes
        Account saved = accountService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(accountMapper.toResponse(saved));
    }

    @Operation(summary = "Update account by IBAN")
    @PutMapping("/{iban}")
    public ResponseEntity<AccountResponse> update(@PathVariable(name = "iban") String iban, @Valid @RequestBody CreateAccountRequest request) {
        Account updated = accountService.update(iban, request);
        return ResponseEntity.ok(accountMapper.toResponse(updated));
    }

    @Operation(summary = "Delete account by IBAN")
    @DeleteMapping("/{iban}")
    public ResponseEntity<Void> delete(@PathVariable(name = "iban") String iban) {
        accountService.delete(iban);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "List accounts by customer id (paginated)")
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<Page<AccountResponse>> getByCustomerId(@PathVariable(name = "customerId") Long customerId, @Parameter(description = "Page request") Pageable pageable) {

        // 1. Buscar Entidades paginadas
        Page<com.paymentchain.transaction.entities.Account> page = accountService.findByCustomerId(customerId, pageable);

        // 2. Convertir a DTOs usando el Mapper existente
        List<AccountResponse> dtos = accountMapper.toResponseList(page.getContent());

        // 3. Responder como Page
        Page<AccountResponse> dtoPage = new PageImpl<>(dtos, pageable, page.getTotalElements());
        return ResponseEntity.ok(dtoPage);
    }

    @Operation(summary = "List all accounts (paginated)")
    @GetMapping
    public ResponseEntity<Page<AccountResponse>> listAll(@Parameter(description = "Page request") Pageable pageable) {
        Page<com.paymentchain.transaction.entities.Account> page = accountService.findAll(pageable);
        List<AccountResponse> dtos = accountMapper.toResponseList(page.getContent());
        Page<AccountResponse> dtoPage = new PageImpl<>(dtos, pageable, page.getTotalElements());
        return ResponseEntity.ok(dtoPage);
    }
}
