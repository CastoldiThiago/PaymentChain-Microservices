package com.paymentchain.customer.dtos;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AccountDto {
    private Long accountId;
    private String iban;
    private BigDecimal balance;
    private String productName;
    private String currency;
}
