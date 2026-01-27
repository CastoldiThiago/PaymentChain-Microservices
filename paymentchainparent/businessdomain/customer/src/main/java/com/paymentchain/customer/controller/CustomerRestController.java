/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/RestController.java to edit this template
 */
package com.paymentchain.customer.controller;

import com.paymentchain.customer.dtos.CustomerFullResponse;
import com.paymentchain.customer.dtos.CustomerRequest;
import com.paymentchain.customer.dtos.CustomerResponse;
import com.paymentchain.customer.entities.Customer;
import com.paymentchain.customer.mapper.CustomerMapper;
import com.paymentchain.customer.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 *
 * @author casto
 */
@RestController
@RequestMapping("/customers")
@RequiredArgsConstructor
public class CustomerRestController {

    private final CustomerService customerService;
    private final CustomerMapper customerMapper;

    // POST: Crear Cliente (y orquestar creación de cuenta)
    @PostMapping
    public ResponseEntity<CustomerResponse> create(@RequestBody CustomerRequest request) {
        // 1. DTO -> Entidad
        Customer entity = customerMapper.toEntity(request);

        // 2. Lógica de Negocio (Guardar y llamar al otro microservicio)
        CustomerResponse created = customerService.createCustomerWithAccount(entity);

        // 3. Entidad -> DTO Respuesta
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // GET: Ver perfil completo con cuentas (BFF)
    @GetMapping("/{id}")
    public ResponseEntity<CustomerFullResponse> getById(@PathVariable(name="id") Long id) {
        CustomerFullResponse response = customerService.getCustomerWithAccounts(id);
        return ResponseEntity.ok(response);
    }

}
