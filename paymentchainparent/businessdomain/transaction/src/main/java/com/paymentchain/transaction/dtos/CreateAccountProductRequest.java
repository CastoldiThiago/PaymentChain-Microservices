package com.paymentchain.transaction.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;


@Data
@Schema(description = "Request to create an Account Product")
public class CreateAccountProductRequest {

    @NotBlank
    @Schema(description = "Product name", example = "Cuenta Corriente Gold")
    private String name;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    @Schema(description = "Transaction fee percentage (0.01 = 1%)", example = "0.01")
    private BigDecimal transactionFeePercentage;

    public void setName(String name) {
        this.name = name;
    }

    public void setTransactionFeePercentage(BigDecimal transactionFeePercentage) {
        this.transactionFeePercentage = transactionFeePercentage;
    }
}
