/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/RestController.java to edit this template
 */
package com.paymentchain.transaction.controller;

import com.paymentchain.transaction.dtos.CreateTransactionRequest;
import com.paymentchain.transaction.dtos.TransactionResponse;
import com.paymentchain.transaction.dtos.TransferRequest;
import com.paymentchain.transaction.dtos.TransferResponseDTO;
import com.paymentchain.transaction.entities.Transaction;
import com.paymentchain.transaction.exception.BusinessRuleException;
import com.paymentchain.transaction.mapper.TransactionMapper;
import com.paymentchain.transaction.service.TransactionService;
import com.paymentchain.transaction.util.SortUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

import java.util.Set;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
@Tag(name = "Transactions", description = "Endpoints to create transactions and view transaction history")
public class TransactionRestController {

    private final TransactionService transactionService;
    private final TransactionMapper transactionMapper;

    // NEW: List transactions (optional account filter)
    @Operation(summary = "List transactions", description = "List all transactions or filter by account IBAN using query param 'accountIban'.")
    @GetMapping
    public ResponseEntity<Page<TransactionResponse>> list(@RequestParam(name = "accountIban", required = false) String accountIban,
                                                           @Parameter(description = "Page request") @PageableDefault(page = 0, size = 20, sort = "date", direction = Sort.Direction.DESC) Pageable pageable,
                                                           @RequestParam(name = "sort", required = false) String[] sortParams) {
        // If client used sort[] or sort params, collect them; else use provided sortParams array
        String[] computedSortParams = sortParams;
        // if caller passed 'sort' variants via query, Spring maps them into request param map, handled earlier in AccountController; here we accept direct param
        Sort defaultSort = Sort.by(Sort.Direction.DESC, "date");
        Sort sort = SortUtils.parseSortParams(computedSortParams, Set.of("date","amount","reference","status"), defaultSort);
        Pageable validated = PageRequest.of(pageable.getPageNumber(), Math.min(pageable.getPageSize(), 100), sort);

        Page<Transaction> page = (accountIban != null && !accountIban.isEmpty())
                ? transactionService.findByAccountIban(accountIban, validated)
                : transactionService.findAll(validated);

        Page<TransactionResponse> dtoPage = page.map(transactionMapper::toResponse);
        return ResponseEntity.ok(dtoPage);
    }

    // NEW: Get transaction by id
    @Operation(summary = "Get transaction by id")
    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> getById(@PathVariable(name = "id") Long id) {
        return transactionService.findById(id)
                .map(transactionMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /transactions - Execute a transaction (deposit/withdrawal)
    @Operation(summary = "Create a transaction (deposit or withdrawal)", description = "Performs a single transaction on the specified account. Returns the created transaction details.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Transaction created successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request - validation failed"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public ResponseEntity<TransactionResponse> performTransaction(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Transaction request payload", required = true, content = @io.swagger.v3.oas.annotations.media.Content)
            @Valid @RequestBody CreateTransactionRequest request) throws BusinessRuleException {
        TransactionResponse transaction = transactionService.performTransaction(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }

    // GET /transactions/account/{iban} - Get account transaction history
    @Operation(summary = "Get transaction history for an account", description = "Returns a list of transactions for the given account IBAN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of transactions returned"),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    @GetMapping("/account/{iban}")
    public ResponseEntity<Page<TransactionResponse>> getHistory(
            @Parameter(description = "IBAN of the account to fetch history for", required = true, example = "AR1769751355549")
            @PathVariable(name = "iban") String iban,
            @Parameter(description = "Page request") @PageableDefault(page = 0, size = 20, sort = "date", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<Transaction> page = transactionService.findByAccountIban(iban, pageable);
        Page<TransactionResponse> dtoPage = page.map(transactionMapper::toResponse);
        return ResponseEntity.ok(dtoPage);
    }

    // POST /transactions/transfer - Internal transfer between two accounts
    @Operation(summary = "Transfer between accounts", description = "Performs an internal transfer from one account to another.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transfer completed successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request - validation failed"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/transfer")
    public ResponseEntity<TransferResponseDTO> transfer(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Transfer request payload", required = true, content = @Content)
            @Valid @RequestBody TransferRequest request) throws BusinessRuleException {
        TransferResponseDTO transfer = transactionService.transfer(request);
        return ResponseEntity.ok(transfer);
    }
}
