package com.paymentchain.customer.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CustomerRequest {
    @NotNull
    @Schema(description = "Name of Customer", example = "Thiago")
    private String name;
    @NotNull
    @Schema(description = "Surname of Customer", example = "Castoldi")
    private String surname;
    @NotNull
    @Schema(description = "Phone number", example = "1153367579")
    private String phone;
    @NotNull
    @Schema(description = "Document ID number of customer", example = "49844274")
    private String dni;
    @NotNull
    @Schema(description = "Email of customer", example = "castoldithiago4@gmail.com")
    private String email;
}
