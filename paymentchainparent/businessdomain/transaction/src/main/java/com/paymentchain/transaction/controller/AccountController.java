/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.paymentchain.transaction.controller;

import com.paymentchain.transaction.entities.Account;
import com.paymentchain.transaction.repository.AccountRepository;
import com.paymentchain.transaction.service.TransactionService;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author casto
 */
@RestController
@RequestMapping("/account")
public class AccountController {
    @Autowired
    AccountRepository accountRepository;
    
    @Autowired
    TransactionService transactionService;
    
    @GetMapping()
    public ResponseEntity<List<Account>> list() {
        List<Account> findAll = accountRepository.findAll();
        if (findAll.isEmpty()){
            return ResponseEntity.noContent().build();
        }else{
            return ResponseEntity.ok(findAll);
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable(name = "id") long id) {
        Optional<Account> transaction = accountRepository.findById(id);
        if (transaction.isPresent()){
            return new ResponseEntity<>(transaction.get(), HttpStatus.OK);
        }else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    
    @PutMapping("/{id}")
    public ResponseEntity<?> put(@PathVariable(name = "id") long id, @RequestBody Account input) {
        Optional<Account> find = accountRepository.findById(id);
        if (find.isEmpty()){
            return ResponseEntity.notFound().build();
        }
        Account findAccount = find.get();
        findAccount.setIban(input.getIban());
        findAccount.setBalance(input.getBalance());
        findAccount.setCustomerId(input.getCustomerId());
        findAccount.setVersion(input.getVersion());
        Account save = accountRepository.save(findAccount);
        return ResponseEntity.ok(save);
    }
    
    @PostMapping
    public ResponseEntity<?> post(@RequestBody Account input){
        Account save = transactionService.createAccount(input);
        return ResponseEntity.ok(save);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable(name = "id") long id) {
        Optional<Account> findById = accountRepository.findById(id);
        if (findById.isPresent()){
            accountRepository.deleteById(id);
        }else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
