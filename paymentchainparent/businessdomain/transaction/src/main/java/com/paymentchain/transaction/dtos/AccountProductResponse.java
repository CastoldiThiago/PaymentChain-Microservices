package com.paymentchain.transaction.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
@Data
@Schema(description = "Account Product response")
public class AccountProductResponse {

    @Schema(description = "Product id", example = "1")
    private Long id;

    @Schema(description = "Product name", example = "Cuenta Corriente Gold")
    private String name;

    @Schema(description = "Transaction fee percentage", example = "0.01")
    private BigDecimal transactionFeePercentage;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getTransactionFeePercentage() {
        return transactionFeePercentage;
    }

    public void setTransactionFeePercentage(BigDecimal transactionFeePercentage) {
        this.transactionFeePercentage = transactionFeePercentage;
    }
}
