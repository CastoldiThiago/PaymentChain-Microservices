/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/RestController.java to edit this template
 */
package com.paymentchain.customer.controller;

import com.paymentchain.customer.businnes.transactions.BusinessTransaction;
import com.paymentchain.customer.entities.Customer;
import com.paymentchain.customer.exception.BusinessRuleException;
import com.paymentchain.customer.repository.CustomerRepository;
import java.net.UnknownHostException;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 *
 * @author casto
 */
@RestController
@RequestMapping("/customer")
public class CustomerRestController {
    
    @Autowired
    CustomerRepository customerRepository;
    
    @Autowired
    BusinessTransaction bt;
    
    
    @GetMapping()
    public ResponseEntity<List<Customer>> list() {
        List<Customer> findAll = customerRepository.findAll();
        if (findAll.isEmpty()){
            return ResponseEntity.noContent().build();
        }else{
            return ResponseEntity.ok(findAll);
        }
        
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable(name = "id") long id) {
        Optional<Customer> customer = customerRepository.findById(id);
        if (customer.isPresent()){
            return new ResponseEntity<>(customer.get(), HttpStatus.OK);
        }else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    @Autowired
    private Environment env;
    
    @GetMapping("/check")
    public String check(){
        return "Hello your property value is: "+env.getProperty("custom.activeprofileName");
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> put(@PathVariable(name = "id") long id, @RequestBody Customer input) {
        Optional<Customer> optionalCustomer = customerRepository.findById(id);
        if (optionalCustomer.isPresent()) {
            Customer save = customerRepository.save(input);
            return new ResponseEntity<>(save, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    @PostMapping
    public ResponseEntity<?> post(@RequestBody Customer input) throws BusinessRuleException, UnknownHostException {
       Customer post = bt.post(input);
       return ResponseEntity.status(HttpStatus.CREATED).body(post);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable(name = "id") long id) {
        Optional<Customer> findById = customerRepository.findById(id);
        if (findById.get() != null){
            customerRepository.delete(findById.get());
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }
    
    @GetMapping("/full")
    public Customer getFullById(@RequestParam(name = "id") Long id) throws UnknownHostException{
        Customer customer = bt.get(id);
        
        return customer;
    }
    
    
}
