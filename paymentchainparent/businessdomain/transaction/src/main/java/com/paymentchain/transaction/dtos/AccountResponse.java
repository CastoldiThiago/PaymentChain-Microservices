package com.paymentchain.transaction.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Account response returned by the Account API")
public class AccountResponse {
    @Schema(description = "Account database id", example = "10")
    private Long accountId;

    @Schema(description = "IBAN of the account", example = "AR1769...")
    private String iban;

    @Schema(description = "Current balance of the account", example = "1000.50")
    private BigDecimal balance;

    @Schema(description = "Customer id owning the account", example = "1")
    private Long customerId;

    @Schema(description = "Currency code of the account", example = "ARS")
    private String currency;

    @Schema(description = "Account product name", example = "Standard Checking")
    private String productName;

    @Schema(description = "Transaction fee percentage applied to transactions for this account", example = "0.005")
    private BigDecimal transactionFee;
}
