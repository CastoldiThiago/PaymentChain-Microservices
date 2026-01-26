/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.paymentchain.billing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 *
 * @author casto
 */
@Schema(description="Represent the data required to create an invoice")
@Data
public class InvoiceRequest {
    
   @NotNull(message = "El ID del cliente es obligatorio")
   @Schema(example="2", description="Unique Id of customer that represent the owner of invoice")
   private long customer;
   @NotNull(message = "El numero es obligatorio")
   @Schema(example="3", description="Number given on fiscal invoice")
   private String number;
   private String detail;
   private double amount;  
}