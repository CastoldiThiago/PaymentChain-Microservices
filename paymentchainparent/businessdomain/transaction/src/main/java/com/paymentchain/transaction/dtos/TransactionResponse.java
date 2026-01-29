package com.paymentchain.transaction.dtos;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransactionResponse {

    private Long transactionId;
    private String accountIban;
    private BigDecimal amount;
    private BigDecimal fee;
    private BigDecimal total;
    private LocalDateTime date;
    private String reference;
    private String status;
}
