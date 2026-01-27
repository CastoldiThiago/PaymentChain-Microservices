/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/RestController.java to edit this template
 */
package com.paymentchain.transaction.controller;

import com.paymentchain.transaction.common.StandarizedApiExceptionResponse;
import com.paymentchain.transaction.dtos.CreateTransactionRequest;
import com.paymentchain.transaction.dtos.TransactionResponse;
import com.paymentchain.transaction.dtos.TransferRequest;
import com.paymentchain.transaction.entities.Transaction;
import com.paymentchain.transaction.exception.BusinessRuleException;
import com.paymentchain.transaction.mapper.TransactionMapper;
import com.paymentchain.transaction.repository.TransactionRepository;
import com.paymentchain.transaction.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionRestController {

    private final TransactionService transactionService;
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;

    // POST /transactions - Ejecutar un movimiento
    @PostMapping
    public ResponseEntity<?> performTransaction(@RequestBody CreateTransactionRequest request) {
        try {
            TransactionResponse transaction = transactionService.performTransaction(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
        } catch (BusinessRuleException ex) {

            StandarizedApiExceptionResponse response = new StandarizedApiExceptionResponse(
                    "/errors/business-rule",
                    "Error de Validación de Negocio",
                    ex.getCode(),
                    ex.getMessage()
            );

            response.setInstance("/transactions");

            return ResponseEntity.status(ex.getHttpStatus()).body(response);
        }
    }

    // GET /transactions/account/{iban} - Ver historial de una cuenta
    @GetMapping("/account/{iban}")
    public ResponseEntity<List<TransactionResponse>> getHistory(@PathVariable(name="iban") String iban) {

        // 1. Buscamos las entidades "sucias" (con relaciones cíclicas) en la BD
        List<Transaction> entities = transactionRepository.findByAccountIban(iban);

        // 2. Las pasamos por el "filtro" del mapper para limpiarlas
        List<TransactionResponse> dtos = transactionMapper.toResponseList(entities);

        // 3. Devolvemos la lista limpia al Frontend
        return ResponseEntity.ok(dtos);
    }

    // Endpoint para Transferencias Internas
    @PostMapping("/transfer")
    public ResponseEntity<?> transfer(@RequestBody TransferRequest request) {
        try {
            transactionService.transfer(request);
            return ResponseEntity.ok().build(); // 200 OK si todo sale bien
        } catch (BusinessRuleException ex) {
            StandarizedApiExceptionResponse response = new StandarizedApiExceptionResponse(
                    "/errors/transfer",
                    "Error en Transferencia",
                    ex.getCode(),
                    ex.getMessage()
            );
            return ResponseEntity.status(ex.getHttpStatus()).body(response);
        }
    }
}
