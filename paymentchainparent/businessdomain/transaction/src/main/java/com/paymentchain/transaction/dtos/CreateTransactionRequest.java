/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.paymentchain.transaction.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 *
 * @author casto
 */
@Data
@Schema(description = "Objeto utilizado para crear una nueva transacción (Depósito o Extracción)")
public class CreateTransactionRequest {

    @NotNull
    @Schema(description = "El IBAN único de la cuenta donde impactará el movimiento", example = "AR0001", requiredMode = Schema.RequiredMode.REQUIRED)
    private String accountIban;

    @NotNull
    @Schema(description = "El monto a procesar. Positivo (+) para Depósito, Negativo (-) para Extracción", example = "100.00", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal amount;

    @Schema(description = "Descripción o referencia del movimiento", example = "Pago de Alquiler de Enero")
    private String reference;
}
