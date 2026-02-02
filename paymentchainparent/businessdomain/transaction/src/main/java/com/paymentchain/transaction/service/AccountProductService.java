package com.paymentchain.transaction.service;

import com.paymentchain.transaction.dtos.AccountProductResponse;
import com.paymentchain.transaction.dtos.CreateAccountProductRequest;
import com.paymentchain.transaction.entities.AccountProduct;
import com.paymentchain.transaction.repository.AccountProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountProductService {

    private final AccountProductRepository productRepository;

    @Transactional
    public AccountProductResponse createProduct(CreateAccountProductRequest request) {
        AccountProduct product = new AccountProduct();
        product.setName(request.getName());
        product.setTransactionFeePercentage(request.getTransactionFeePercentage());
        AccountProduct saved = productRepository.save(product);

        AccountProductResponse res = new AccountProductResponse();
        res.setId(saved.getId());
        res.setName(saved.getName());
        res.setTransactionFeePercentage(saved.getTransactionFeePercentage());
        return res;
    }

    public List<AccountProductResponse> getAllProducts() {
        List<AccountProduct> products = productRepository.findAll();
        return products.stream().map(p -> {
            AccountProductResponse r = new AccountProductResponse();
            r.setId(p.getId());
            r.setName(p.getName());
            r.setTransactionFeePercentage(p.getTransactionFeePercentage());
            return r;
        }).collect(Collectors.toList());
    }
}
