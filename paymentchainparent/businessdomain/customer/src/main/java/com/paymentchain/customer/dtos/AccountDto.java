package com.paymentchain.customer.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Account information returned in customer full profile")
public class AccountDto {
    @Schema(description = "Account internal id", example = "10")
    private Long accountId;

    @Schema(description = "IBAN of the account", example = "AR1769...")
    private String iban;

    @Schema(description = "Current balance", example = "1000.50")
    private BigDecimal balance;

    @Schema(description = "Product name of the account", example = "Standard Checking")
    private String productName;

    @Schema(description = "Currency of the account", example = "ARS")
    private String currency;
}
