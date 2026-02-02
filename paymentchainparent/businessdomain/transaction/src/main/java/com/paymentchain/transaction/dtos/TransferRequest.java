package com.paymentchain.transaction.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Request object for transferring money between two accounts")
public class TransferRequest {

    @NotNull
    @Schema(description = "Source Account IBAN (who send money)", example = "AR0001", requiredMode = Schema.RequiredMode.REQUIRED)
    private String sourceIban;

    @NotNull
    @Schema(description = "Target account IBAN (who receive money)", example = "AR0002", requiredMode = Schema.RequiredMode.REQUIRED)
    private String targetIban;

    @NotNull
    @Min(value = 1, message = "Amount to transfer, must be positive")
    @Schema(description = "Transfer amount, always positive", example = "500.00", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal amount;

    @Schema(description = "Reference or description of the transaction", example = "Birthday gift")
    private String reference;
}
