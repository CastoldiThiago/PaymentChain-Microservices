// ...existing code...
package com.paymentchain.transaction.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response for a transfer operation, including debit and credit transaction details.")
public class TransferResponseDTO {
    @Schema(description = "Debit transaction details (money sent)")
    private TransactionResponse debitTransaction;

    @Schema(description = "Credit transaction details (money received)")
    private TransactionResponse creditTransaction;

    @Schema(description = "Status of the transfer operation")
    private String status;

    @Schema(description = "Reference for the transfer")
    private String reference;
}
// ...existing code...
