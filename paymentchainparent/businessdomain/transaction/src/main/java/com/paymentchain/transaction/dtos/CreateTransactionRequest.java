/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.paymentchain.transaction.dtos;

import com.paymentchain.transaction.enums.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 *
 * @author casto
 */
@Data
@Schema(description = "Request object for creating a new transaction: deposit or withdrawal")
public class CreateTransactionRequest {

    @NotNull
    @Schema(description = "Unique IBAN of the account in which transaction will impact", example = "AR0001", requiredMode = Schema.RequiredMode.REQUIRED)
    private String accountIban;

    @NotNull
    @Schema(description = "Amount to process, only positive", example = "100.00", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal amount;

    @Schema(description = "movement description or reference", example = "Salary payment")
    private String reference;

    @NotNull
    @Schema(description = "Original payment currency", example = "USD")
    private String currency;

    @NotNull
    @Schema(description = "Transaction type: DEPOSIT o WITHDRAWAL", example = "DEPOSIT", requiredMode = Schema.RequiredMode.REQUIRED)
    private TransactionType type;
}
