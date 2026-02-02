package com.paymentchain.customer.mapper;

import com.paymentchain.customer.dtos.AccountDto;
import com.paymentchain.customer.dtos.CustomerFullResponse;
import com.paymentchain.customer.dtos.CustomerRequest;
import com.paymentchain.customer.dtos.CustomerResponse;
import com.paymentchain.customer.entities.Customer;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CustomerMapper {

    // Request (DTO) -> Entity
    public Customer toEntity(CustomerRequest request) {
        Customer customer = new Customer();
        customer.setName(request.getName());
        customer.setSurname(request.getSurname());
        customer.setPhone(request.getPhone());
        customer.setDni(request.getDni());
        customer.setEmail(request.getEmail());
        return customer;
    }

    // Entity -> Response (DTO)
    public CustomerResponse toResponse(Customer entity) {
        CustomerResponse dto = new CustomerResponse();
        dto.setCustomerId(entity.getId());
        dto.setName(entity.getName());
        dto.setSurname(entity.getSurname());
        dto.setDni(entity.getDni());
        dto.setEmail(entity.getEmail());
        dto.setStatus(entity.getStatus());
        return dto;
    }

    public List<CustomerResponse> toResponseList(List<Customer> entities) {
        return entities.stream().map(this::toResponse).collect(Collectors.toList());
    }

    // Entity + Lista Cuentas -> Full Response (DTO Completo)
    public CustomerFullResponse toFullResponse(Customer entity, List<AccountDto> accounts) {
        CustomerFullResponse dto = new CustomerFullResponse();
        dto.setCustomerId(entity.getId());
        dto.setName(entity.getName());
        dto.setSurname(entity.getSurname());
        dto.setDni(entity.getDni());
        dto.setStatus(entity.getStatus());
        dto.setEmail(entity.getEmail());
        dto.setAccounts(accounts);
        return dto;
    }
}
