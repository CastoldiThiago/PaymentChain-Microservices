package com.paymentchain.customer.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AccountRequest {
    @NotNull
    @Schema(description = "Account ID", example = "AR0001123")
    private String iban;
    @Schema(description = "Initial account balance", example = "1500.75")
    private BigDecimal balance;
    @NotNull
    @Schema(description = "Customer number id", example = "1")
    private Long customerId;
    @NotNull
    @Schema(description = "Product (account type) number id", example = "1")
    private Long productId;
    @NotNull
    @Schema(description = "Account currency", example = "USD")
    private String currency;
}
