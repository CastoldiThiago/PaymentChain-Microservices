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
import com.paymentchain.transaction.util.SortUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
    public ResponseEntity<AccountResponse> create(@Valid @RequestBody CreateAccountRequest request) {
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

    @Operation(summary = "List accounts by customer id")
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<AccountResponse>> getByCustomerId(@PathVariable(name = "customerId") Long customerId) {
        List<Account> accounts = accountService.findByCustomerId(customerId);
        List<AccountResponse> dtos = accountMapper.toResponseList(accounts);
        return ResponseEntity.ok(dtos);
    }

    @Operation(summary = "List all accounts (paginated)")
    @GetMapping
    public ResponseEntity<Page<AccountResponse>> listAll(@Parameter(description = "Page request") @PageableDefault(page = 0, size = 20, sort = "iban", direction = Sort.Direction.ASC) Pageable pageable, @RequestParam Map<String, String[]> allRequestParams) {
        String[] sortParams = allRequestParams.entrySet().stream()
                .filter(e -> e.getKey() != null && e.getKey().toLowerCase().startsWith("sort"))
                .flatMap(e -> java.util.Arrays.stream(e.getValue()))
                .toArray(String[]::new);
        Sort defaultSort = Sort.by(Sort.Direction.ASC, "iban");
        Sort sort = SortUtils.parseSortParams(sortParams, java.util.Set.of("iban","balance","customerId","currency"), defaultSort);
        Pageable validated = PageRequest.of(pageable.getPageNumber(), Math.min(pageable.getPageSize(), 100), sort);

        Page<Account> page = accountService.findAll(validated);
        Page<AccountResponse> dtoPage = page.map(accountMapper::toResponse);
        return ResponseEntity.ok(dtoPage);
    }
}
