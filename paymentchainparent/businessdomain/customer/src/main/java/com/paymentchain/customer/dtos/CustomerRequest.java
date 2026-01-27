package com.paymentchain.customer.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CustomerRequest {
    @NotNull
    private String name;
    @NotNull
    private String surname;
    @NotNull
    private String phone;
    @NotNull
    private String dni;
    @NotNull
    private String email;
}
