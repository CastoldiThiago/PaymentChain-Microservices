package com.paymentchain.customer.service;

import com.paymentchain.customer.dtos.AccountDto;
import com.paymentchain.customer.dtos.CustomerFullResponse;
import com.paymentchain.customer.dtos.CustomerResponse;
import com.paymentchain.customer.entities.Customer;
import com.paymentchain.customer.mapper.CustomerMapper;
import com.paymentchain.customer.repository.CustomerRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final WebClient.Builder webClientBuilder;
    private final CustomerMapper customerMapper;

    public CustomerResponse create(Customer customer) {

        // Pre-insert validations: prevent duplicate email/dni with a clear 409
        if (customer.getEmail() != null && customerRepository.existsByEmail(customer.getEmail())) {
            throw new com.paymentchain.customer.exception.DuplicateResourceException("email", "Email already exists");
        }
        if (customer.getDni() != null && customerRepository.existsByDni(customer.getDni())) {
            throw new com.paymentchain.customer.exception.DuplicateResourceException("dni", "DNI already exists");
        }

        customer.setStatus("CREATED");
        Customer savedCustomer = customerRepository.save(customer);

        return customerMapper.toResponse(savedCustomer);


    }

    public CustomerFullResponse getCustomerWithAccounts(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        // CORRECCIÓN DE URL PARA DOCKER
        String transactionUrl = "http://transaction-service:8083/accounts/customer/" + customerId;

        List<AccountDto> accounts = webClientBuilder.build()
                .get()
                .uri(transactionUrl)
                .retrieve()
                .bodyToFlux(AccountDto.class)
                .collectList()
                .block();

        return customerMapper.toFullResponse(customer, accounts);
    }

    // New helper methods for CRUD
    public List<Customer> findAll() {
        return customerRepository.findAll();
    }

    public Page<Customer> findAll(Pageable pageable) {
        return customerRepository.findAll(pageable);
    }

    @Transactional
    public Customer update(Long id, Customer updated) {
        Customer existing = customerRepository.findById(id).orElseThrow(() -> new RuntimeException("Customer not found"));
        existing.setName(updated.getName());
        existing.setSurname(updated.getSurname());
        existing.setPhone(updated.getPhone());
        existing.setDni(updated.getDni());
        existing.setEmail(updated.getEmail());
        return customerRepository.save(existing);
    }

    public void delete(Long id) {
        if (!customerRepository.existsById(id)) {
            throw new RuntimeException("Customer not found");
        }
        customerRepository.deleteById(id);
    }
}
