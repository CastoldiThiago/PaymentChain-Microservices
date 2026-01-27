package com.paymentchain.transaction.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Objeto utilizado para realizar transferencias entre dos cuentas internas")
public class TransferRequest {

    @NotNull
    @Schema(description = "IBAN de la cuenta de origen (quien envía el dinero)", example = "AR0001", requiredMode = Schema.RequiredMode.REQUIRED)
    private String sourceIban;

    @NotNull
    @Schema(description = "IBAN de la cuenta de destino (quien recibe el dinero)", example = "AR0002", requiredMode = Schema.RequiredMode.REQUIRED)
    private String targetIban;

    @NotNull
    @Min(value = 1, message = "El monto de transferencia debe ser mayor a 0")
    @Schema(description = "Monto a transferir (Debe ser positivo)", example = "500.00", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal amount;

    @Schema(description = "Referencia o concepto de la transferencia", example = "Regalo de cumpleaños")
    private String reference;
}
