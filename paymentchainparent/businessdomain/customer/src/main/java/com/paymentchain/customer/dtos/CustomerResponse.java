package com.paymentchain.customer.dtos;

import lombok.Data;

@Data
public class CustomerResponse {
    private Long customerId;
    private String name;
    private String surname;
    private String dni;
    private String status;
    private String assignedIban;
}
