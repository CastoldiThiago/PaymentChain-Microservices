package com.paymentchain.customer.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AccountRequest {
    private String iban;
    private BigDecimal balance;
    private Long customerId;
    private Long productId; // ID del producto (ej: 1 = Caja Ahorro)
    @NotNull
    @Schema(description = "Moneda de la cuenta", example = "USD")
    private String currency;
}
