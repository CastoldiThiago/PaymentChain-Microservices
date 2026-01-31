package com.paymentchain.transaction.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "Transaction details returned by Transactions API")
public class TransactionResponse {

    @Schema(description = "Transaction identifier", example = "100")
    private Long transactionId;

    @Schema(description = "Account IBAN affected by the transaction", example = "AR1769751355549")
    private String accountIban;

    @Schema(description = "Amount (signed). Positive for credit, negative for debit", example = "1000.00")
    private BigDecimal amount;

    @Schema(description = "Applied transaction fee", example = "5.00")
    private BigDecimal fee;

    @Schema(description = "Total effect over the account after fee", example = "995.00")
    private BigDecimal total;

    @Schema(description = "Date and time when transaction was executed")
    private LocalDateTime date;

    @Schema(description = "Reference or description of the transaction", example = "Salary payment")
    private String reference;

    @Schema(description = "Transaction status", example = "COMPLETED")
    private String status;

    @Schema(description = "Currency in which the transaction was performed", example = "ARS")
    private String currency;
}
