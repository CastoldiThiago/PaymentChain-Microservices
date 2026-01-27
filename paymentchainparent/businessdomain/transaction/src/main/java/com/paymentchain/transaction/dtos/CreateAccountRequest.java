package com.paymentchain.transaction.dtos;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateAccountRequest {
    private String iban;
    private BigDecimal balance;
    private Long customerId;
    private Long productId; // <--- Fíjate que aquí pedimos solo el ID (Long), no el objeto.
}
