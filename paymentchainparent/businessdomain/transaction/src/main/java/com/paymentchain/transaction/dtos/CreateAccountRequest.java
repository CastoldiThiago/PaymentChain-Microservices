package com.paymentchain.transaction.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Request object for creating a new bank account")
public class CreateAccountRequest {
    @Schema(description = "IBAN of the new account", example = "AR1769751355549")
    private String iban;

    @Schema(description = "Initial balance of the account", example = "1000.00")
    private BigDecimal balance;

    @Schema(description = "Customer ID owning the account", example = "1")
    private Long customerId;

    @Schema(description = "Product ID associated with the account", example = "5")
    private Long productId;

    @Schema(description = "Account currency", example = "USD")
    private String currency;
}
