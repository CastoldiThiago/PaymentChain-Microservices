package com.paymentchain.transaction.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateAccountRequest {
    private String iban;
    private BigDecimal balance;
    private Long customerId;
    private Long productId;

    @Schema(description = "Moneda de la cuenta", example = "USD")
    private String currency;
}
