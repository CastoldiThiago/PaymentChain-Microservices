package com.paymentchain.customer.dtos;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AccountRequest {
    private String iban;
    private BigDecimal balance;
    private Long customerId;
    private Long productId; // ID del producto (ej: 1 = Caja Ahorro)
}
