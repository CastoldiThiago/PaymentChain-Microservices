package com.paymentchain.transaction.dtos;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AccountResponse {
    private Long accountId;
    private String iban;
    private BigDecimal balance;
    private Long customerId;

    // Aplanamos la info del producto para no devolver objetos complejos
    private String productCode;
    private String productName;
    private BigDecimal transactionFee;
}
