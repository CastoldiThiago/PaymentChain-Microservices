/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.paymentchain.transaction.dtos;

import com.paymentchain.transaction.entities.Channel;
import com.paymentchain.transaction.entities.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author casto
 */
@Data
@Builder // Patrón Builder para facilitar la creación en el mapper
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDetailDTO {

    @Schema(description = "ID único de la transacción", example = "105")
    private Long id;

    @Schema(description = "Concepto o referencia breve", example = "Retiro Cajero")
    private String reference;

    // --- EL TRUCO PARA EVITAR EL ERROR DE HIBERNATE ---
    // No devolvemos el objeto Account entero, solo el IBAN.
    @Schema(description = "IBAN de la cuenta asociada", example = "AR00000123...")
    private String accountIban; 

    @Schema(description = "Fecha y hora de la transacción")
    private LocalDateTime date;

    @Schema(description = "Monto total debitado/acreditado (incluye comisión)", example = "-100.98")
    private BigDecimal amount;

    @Schema(description = "Comisión aplicada (informativo)", example = "0.98")
    private BigDecimal fee;

    @Schema(description = "Descripción detallada", example = "Transacción procesada correctamente")
    private String description;

    @Schema(description = "Estado de la transacción", example = "LIQUIDADA")
    private Status status;
    
    @Schema(description = "Canal de origen", example = "CAJERO")
    private Channel channel;
}
