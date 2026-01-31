package com.paymentchain.notification.client.dto;

import lombok.Data;

@Data
public class CustomerResponseDto {
    private Long customerId;
    private String name;
    private String surname;
    private String email;
}
