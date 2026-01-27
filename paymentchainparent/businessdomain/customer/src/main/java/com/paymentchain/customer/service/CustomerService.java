package com.paymentchain.customer.service;

import com.paymentchain.customer.dtos.AccountDto;
import com.paymentchain.customer.dtos.AccountRequest;
import com.paymentchain.customer.dtos.CustomerFullResponse;
import com.paymentchain.customer.dtos.CustomerResponse;
import com.paymentchain.customer.entities.Customer;
import com.paymentchain.customer.mapper.CustomerMapper;
import com.paymentchain.customer.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final WebClient.Builder webClientBuilder; // Inyectamos el cliente HTTP

    private final CustomerMapper customerMapper;



    public CustomerResponse createCustomerWithAccount(Customer customer) {

        // 1. Guardar Cliente (Sin IBAN todavía)
        customer.setStatus("CREATED");
        Customer savedCustomer = customerRepository.save(customer);

        // 2. Generar IBAN y llamar al otro Microservicio
        String newIban = "AR" + System.currentTimeMillis();

        AccountRequest accountReq = new AccountRequest();
        accountReq.setCustomerId(savedCustomer.getId());
        accountReq.setIban(newIban);
        accountReq.setBalance(BigDecimal.ZERO);
        accountReq.setProductId(1L);

        try {
            webClientBuilder.build()
                    .post()
                    .uri("http://localhost:8083/accounts") // URL Transaction
                    .bodyValue(accountReq)
                    .retrieve()
                    .bodyToMono(Object.class)
                    .block();

            // 3. Actualizar estado a ACTIVE
            savedCustomer.setStatus("ACTIVE");
            savedCustomer = customerRepository.save(savedCustomer);

            return customerMapper.toResponse(savedCustomer, newIban);

        } catch (Exception e) {
            savedCustomer.setStatus("ERROR_ACCOUNT");
            customerRepository.save(savedCustomer);
            throw new RuntimeException("Error creando cuenta: " + e.getMessage());
        }
    }

    public CustomerFullResponse getCustomerWithAccounts(Long customerId) {
        // 1. Buscamos datos personales (Local)
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        // 2. Buscamos sus cuentas (Remoto)
        // TIPADO FUERTE: .bodyToFlux(AccountDto.class)
        List<AccountDto> accounts = webClientBuilder.build()
                .get()
                .uri("http://localhost:8083/accounts/customer/" + customerId)
                .retrieve()
                .bodyToFlux(AccountDto.class) // <--- Aquí ocurre la magia de conversión JSON -> Java
                .collectList()
                .block();

        // 3. Unificamos usando el Mapper
        return customerMapper.toFullResponse(customer, accounts);
    }
}
