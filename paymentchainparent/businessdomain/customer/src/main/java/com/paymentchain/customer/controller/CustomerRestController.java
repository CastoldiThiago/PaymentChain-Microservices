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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/customers")
@RequiredArgsConstructor
@Tag(name = "Customers", description = "Customer management APIs")
public class CustomerRestController {

    private final CustomerService customerService;
    private final CustomerMapper customerMapper;

    @Operation(summary = "Create a new customer")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Customer created"), @ApiResponse(responseCode = "400", description = "Invalid input")})
    @PostMapping
    public ResponseEntity<CustomerResponse> create(@Valid @RequestBody CustomerRequest request) {
        Customer entity = customerMapper.toEntity(request);
        CustomerResponse created = customerService.create(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Get full customer profile including accounts (BFF)")
    @GetMapping("/{id}")
    public ResponseEntity<CustomerFullResponse> getById(@PathVariable(name = "id") Long id) {
        CustomerFullResponse response = customerService.getCustomerWithAccounts(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "List customers with pagination")
    @GetMapping
    public ResponseEntity<Page<CustomerResponse>> listAll(@Parameter(description = "Page request") @PageableDefault(page = 0, size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<com.paymentchain.customer.entities.Customer> page = customerService.findAll(pageable);
        Page<CustomerResponse> dtoPage = page.map(customerMapper::toResponse);
        return ResponseEntity.ok(dtoPage);
    }

    @Operation(summary = "Update a customer by id")
    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponse> update(@PathVariable(name = "id") Long id, @Valid @RequestBody CustomerRequest request) {
        Customer updated = customerMapper.toEntity(request);
        CustomerResponse response = customerMapper.toResponse(customerService.update(id, updated));
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete a customer by id")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Deleted")})
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable(name = "id") Long id) {
        customerService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
