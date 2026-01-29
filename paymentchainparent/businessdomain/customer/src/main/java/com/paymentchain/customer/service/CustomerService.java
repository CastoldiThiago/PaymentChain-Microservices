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
    private final WebClient.Builder webClientBuilder;
    private final CustomerMapper customerMapper;

    public CustomerResponse createCustomerWithAccount(Customer customer) {

        // 1. Guardar Cliente
        customer.setStatus("CREATED");
        Customer savedCustomer = customerRepository.save(customer);

        // 2. Preparar Request para el otro microservicio
        String newIban = "AR" + System.currentTimeMillis();

        AccountRequest accountReq = new AccountRequest();
        accountReq.setCustomerId(savedCustomer.getId());
        accountReq.setIban(newIban);
        accountReq.setBalance(BigDecimal.ZERO);
        // NUEVO: Definir la moneda por defecto (ARS)
        accountReq.setCurrency("ARS");
        accountReq.setProductId(1L);

        try {
            // CORRECCIÓN DE URL PARA DOCKER
            // Usamos el nombre del contenedor definido en docker-compose ("transaction-service")
            // Asumimos que dentro del contenedor corre en el puerto 8080 (default de Spring)
            String transactionUrl = "http://transaction-service:8083/accounts";
            // OJO: Verifica si tu Controller tiene @RequestMapping("/transaction") o directo "/accounts"

            webClientBuilder.build()
                    .post()
                    .uri(transactionUrl)
                    .bodyValue(accountReq)
                    .retrieve()
                    .bodyToMono(Object.class)
                    .block();

            // 3. Confirmar éxito
            savedCustomer.setStatus("ACTIVE");
            savedCustomer = customerRepository.save(savedCustomer);

            return customerMapper.toResponse(savedCustomer, newIban);

        } catch (Exception e) {
            // COMPENSATING TRANSACTION: Si falla la cuenta, marcamos error en cliente
            savedCustomer.setStatus("ERROR_ACCOUNT");
            customerRepository.save(savedCustomer);
            throw new RuntimeException("Error creando cuenta en microservicio Transaction: " + e.getMessage());
        }
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
}
