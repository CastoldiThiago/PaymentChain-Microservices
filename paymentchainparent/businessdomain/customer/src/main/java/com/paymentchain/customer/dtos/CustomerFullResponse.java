package com.paymentchain.customer.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Full customer profile including associated accounts")
public class CustomerFullResponse {
    @Schema(description = "Internal customer identifier", example = "1")
    private Long customerId;

    @Schema(description = "First name of the customer", example = "Thiago")
    private String name;

    @Schema(description = "Last name of the customer", example = "Castoldi")
    private String surname;

    @Schema(description = "Document (DNI) number", example = "49844274")
    private String dni;

    @Schema(description = "Email address of the customer", example = "catr@gmail.com")
    private String email;

    @Schema(description = "Customer status", example = "ACTIVE")
    private String status;

    @Schema(description = "List of accounts associated with the customer")
    private List<AccountDto> accounts;
}
