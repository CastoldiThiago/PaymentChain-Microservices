package com.paymentchain.transaction.controller;

import com.paymentchain.transaction.dtos.AccountProductResponse;
import com.paymentchain.transaction.dtos.CreateAccountProductRequest;
import com.paymentchain.transaction.service.AccountProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/account-products")
@RequiredArgsConstructor
@Tag(name = "AccountProducts", description = "Manage account products (definitions of account types)")
public class AccountProductController {

    private final AccountProductService productService;

    @Operation(summary = "Create account product")
    @PostMapping
    public ResponseEntity<AccountProductResponse> createProduct(@Valid @RequestBody CreateAccountProductRequest request) {
        AccountProductResponse res = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @Operation(summary = "Get all account products", description = "Returns the list of account products (no pagination)")
    @GetMapping
    public ResponseEntity<List<AccountProductResponse>> getAll() {
        List<AccountProductResponse> list = productService.getAllProducts();
        return ResponseEntity.ok(list);
    }
}
