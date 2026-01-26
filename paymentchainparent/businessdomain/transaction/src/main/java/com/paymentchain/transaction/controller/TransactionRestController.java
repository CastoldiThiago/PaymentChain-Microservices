/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/RestController.java to edit this template
 */
package com.paymentchain.transaction.controller;

import com.paymentchain.transaction.dtos.CreateTransactionRequest;
import com.paymentchain.transaction.dtos.TransactionDetailDTO;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import com.paymentchain.transaction.entities.Transaction;
import com.paymentchain.transaction.exception.BusinessRuleException;
import com.paymentchain.transaction.repository.TransactionRepository;
import com.paymentchain.transaction.service.TransactionService;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;

/**
 * REST controller for transaction management.
 * <p>
 * Exposes CRUD and query endpoints for Transactions. Methods return DTOs
 * and use the service/repository for mapping and business logic.
 */
@Tag(name = "Transaction API", description = "API for managing transactions")
@RestController
@RequestMapping("/transaction")
public class TransactionRestController {
    
    @Autowired
    TransactionRepository transactionRepository;
    
    @Autowired
    TransactionService transactionService;
    
    /**
     * List all transactions.
     * Returns 200 with the list of TransactionDetailDTO or 204 No Content when empty.
     */
    @Operation(summary = "List transactions", description = "Returns all transactions. Responds with 204 No Content when there are none.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List of transactions"),
        @ApiResponse(responseCode = "204", description = "No content"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping()
    public ResponseEntity<List<TransactionDetailDTO>> list() {
        List<Transaction> findAll = transactionRepository.findAll();
        if (findAll.isEmpty()){
            return ResponseEntity.noContent().build();
        }else{
            List<TransactionDetailDTO> dtos = findAll.stream()
                    .map(transactionService::mapToDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        }
    }
    
    /**
     * Get a transaction by its id.
     * @param id Transaction identifier
     * @return 200 with TransactionDetailDTO or 404 if not found
     */
    @Operation(summary = "Get transaction by id", description = "Retrieve a transaction by its id")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Transaction found"),
        @ApiResponse(responseCode = "404", description = "Transaction not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<TransactionDetailDTO> get(
            @Parameter(in = ParameterIn.PATH, description = "Transaction ID", required = true)
            @PathVariable(name = "id") long id) {
        Optional<Transaction> transaction = transactionRepository.findById(id);
        if (transaction.isPresent()){
            TransactionDetailDTO dto = transactionService.mapToDTO(transaction.get());
            return ResponseEntity.ok(dto);
        }else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    /**
     * Get transactions for a customer (by customerId query param).
     * @param customerId customer identifier
     * @return 200 with list of TransactionDetailDTO or 204 when no results
     */
    @Operation(summary = "Get transactions by customer", description = "Return transactions associated with a customerId")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List of transactions for the customer"),
        @ApiResponse(responseCode = "204", description = "No content"),
        @ApiResponse(responseCode = "400", description = "Invalid parameters")
    })
    @GetMapping("/customer/transactions")
    public ResponseEntity<List<TransactionDetailDTO>> get(
            @Parameter(in = ParameterIn.QUERY, description = "Customer ID to filter transactions", required = true)
            @RequestParam(name = "customerId") Long customerId) {
        List<TransactionDetailDTO> transactionsByCustomer = transactionService.findTransactionsByCustomerId(customerId);
        if (transactionsByCustomer.isEmpty()){
            return ResponseEntity.noContent().build();
        }else{
            return ResponseEntity.ok(transactionsByCustomer);
        }
    }
    
    /**
     * Update an existing transaction identified by id.
     * @param id Transaction identifier to update
     * @param input Transaction entity with the values to update (allowed fields are updated)
     * @return 200 with the updated entity or 404 if not found
     */
    @Operation(summary = "Update transaction", description = "Update an existing transaction. Returns 404 if not found.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Transaction updated"),
        @ApiResponse(responseCode = "404", description = "Transaction not found"),
        @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> put(
            @Parameter(in = ParameterIn.PATH, description = "Transaction ID", required = true)
            @PathVariable(name = "id") long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Payload with transaction data to update", required = true)
            @RequestBody Transaction input) {
        Optional<Transaction> findT = transactionRepository.findById(id);
        if (findT.isEmpty()){
            return ResponseEntity.notFound().build();
        }
        Transaction find = findT.get();
        find.setAmount(input.getAmount());
        find.setChannel(input.getChannel());
        find.setDate(input.getDate());
        find.setDescription(input.getDescription());
        find.setFee(input.getFee());
        find.setAccount(input.getAccount());
        find.setReference(input.getReference());
        find.setStatus(input.getStatus());
        Transaction save = transactionRepository.save(find);
        return ResponseEntity.ok(save);
    }
    
    /**
     * Execute a transaction according to the request (business logic in TransactionService).
     * @param input DTO containing the information required to create the transaction
     * @return 200 with resulting TransactionDetailDTO or appropriate 4xx/5xx for errors
     */
    @Operation(summary = "Perform transaction", description = "Create/execute a transaction according to business rules")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Transaction executed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "422", description = "Business rule violation (BusinessRuleException)")
    })
    @PostMapping
    public ResponseEntity<TransactionDetailDTO> post(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Data to create the transaction", required = true)
            @RequestBody CreateTransactionRequest input) throws BusinessRuleException {
        TransactionDetailDTO save = transactionService.performTransaction(input);
        return ResponseEntity.ok(save);
    }
    
    /**
     * Delete a transaction by id.
     * @param id Transaction identifier to delete
     * @return 200 if deleted or 404 if not found
     */
    @Operation(summary = "Delete transaction", description = "Delete a transaction by its id")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Transaction not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(
            @Parameter(in = ParameterIn.PATH, description = "Transaction ID", required = true)
            @PathVariable(name = "id") long id) {
        Optional<Transaction> findById = transactionRepository.findById(id);
        if (findById.isPresent()){
            transactionRepository.deleteById(id);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }
    
}
