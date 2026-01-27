package com.paymentchain.customer.dtos;

import lombok.Data;

import java.util.List;

@Data
public class CustomerFullResponse {
    private Long customerId;
    private String name;
    private String surname;
    private String dni;
    private String status;
    private List<AccountDto> accounts;
}
