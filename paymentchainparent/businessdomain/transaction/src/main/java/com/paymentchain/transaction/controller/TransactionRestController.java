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

/**
 *
 * @author casto
 */
@RestController
@RequestMapping("/transaction")
public class TransactionRestController {
    
    @Autowired
    TransactionRepository transactionRepository;
    
    @Autowired
    TransactionService transactionService;
    
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
    
    @GetMapping("/{id}")
    public ResponseEntity<TransactionDetailDTO> get(@PathVariable(name = "id") long id) {
        Optional<Transaction> transaction = transactionRepository.findById(id);
        if (transaction.isPresent()){
            TransactionDetailDTO dto = transactionService.mapToDTO(transaction.get());
            return ResponseEntity.ok(dto);
        }else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    @GetMapping("/customer/transactions")
    public ResponseEntity<List<TransactionDetailDTO>> get(@RequestParam(name = "customerId") Long customerId) {
        List<TransactionDetailDTO> transactionsByCustomer = transactionService.findTransactionsByCustomerId(customerId);
        if (transactionsByCustomer.isEmpty()){
            return ResponseEntity.noContent().build();
        }else{
            return ResponseEntity.ok(transactionsByCustomer);
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> put(@PathVariable(name = "id") long id, @RequestBody Transaction input) {
        Transaction find = transactionRepository.findById(id).get();
        if (find != null) {
            find.setAmount(input.getAmount());
            find.setChannel(input.getChannel());
            find.setDate(input.getDate());
            find.setDescription(input.getDescription());
            find.setFee(input.getFee());
            find.setAccount(input.getAccount());
            find.setReference(input.getReference());
            find.setStatus(input.getStatus());
        } else {
            return ResponseEntity.notFound().build();
        }
        Transaction save = transactionRepository.save(find);
        return ResponseEntity.ok(save);
    }
    
    @PostMapping
    public ResponseEntity<TransactionDetailDTO> post(@RequestBody CreateTransactionRequest input) throws BusinessRuleException {
        TransactionDetailDTO save = transactionService.performTransaction(input);
        return ResponseEntity.ok(save);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable(name = "id") long id) {
        Optional<Transaction> findById = transactionRepository.findById(id);
        if (findById.get() != null){
            transactionRepository.delete(findById.get());
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }
    
}
