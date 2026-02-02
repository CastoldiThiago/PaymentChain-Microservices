package com.paymentchain.notification.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "Transaction details used by Notification Service")
public class TransactionResponse {

    @Schema(description = "Transaction identifier", example = "100")
    private Long transactionId;

    @Schema(description = "Account IBAN affected by the transaction", example = "AR1769751355549")
    private String accountIban;

    @Schema(description = "Amount (positive)", example = "1000.00")
    private BigDecimal amount;

    @Schema(description = "Applied fee", example = "5.00")
    private BigDecimal fee;

    @Schema(description = "Total effect on the account after fee", example = "995.00")
    private BigDecimal total;

    @Schema(description = "Execution date/time")
    private LocalDateTime date;

    @Schema(description = "Reference or description", example = "Salary payment")
    private String reference;

    @Schema(description = "Transaction status", example = "COMPLETED")
    private String status;

    @Schema(description = "Currency code", example = "ARS")
    private String currency;

    @Schema(description = "Owner customer id", example = "1")
    private Long customerId;

    @Schema(description = "Transaction type", example = "DEPOSIT")
    private TransactionType type;
}
