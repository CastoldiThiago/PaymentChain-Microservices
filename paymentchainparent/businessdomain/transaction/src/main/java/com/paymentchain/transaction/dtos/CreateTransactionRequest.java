/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.paymentchain.transaction.dtos;

import com.paymentchain.transaction.entities.Channel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author casto
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTransactionRequest {

    // 1. Usamos IBAN, no el ID de base de datos. Es más seguro y real.
    @NotBlank(message = "El IBAN de la cuenta es obligatorio")
    @Schema(description = "IBAN de la cuenta a operar", example = "AR00000000000000123456")
    private String accountIban;

    // 2. Permitimos negativos (retiros) y positivos (depósitos)
    @NotNull(message = "El monto es obligatorio")
    @Schema(description = "Monto de la operación. Negativo para débito, positivo para crédito", example = "-100.00")
    private BigDecimal amount;

    @NotBlank(message = "La referencia es obligatoria")
    @Schema(description = "Descripción corta o referencia del movimiento", example = "Pago Alquiler")
    private String reference;
    
    @Schema(description = "Descripción del movimiento", example = "Pago adelantado Alquiler del 15/06/2026")
    private String description;

    @Schema(description = "Canal desde donde se origina la transacción", example = "WEB")
    private Channel channel;
}
