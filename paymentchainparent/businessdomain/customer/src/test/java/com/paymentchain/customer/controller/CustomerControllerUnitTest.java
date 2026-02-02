package com.paymentchain.customer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paymentchain.customer.dtos.CustomerRequest;
import com.paymentchain.customer.dtos.CustomerResponse;
import com.paymentchain.customer.entities.Customer;
import com.paymentchain.customer.mapper.CustomerMapper;
import com.paymentchain.customer.service.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class CustomerControllerUnitTest {

    private MockMvc mockMvc;

    @Mock
    private CustomerService customerService;

    @Mock
    private CustomerMapper customerMapper;

    @InjectMocks
    private com.paymentchain.customer.controller.CustomerRestController customerRestController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        // register Pageable resolver so controller's Pageable parameter is resolved in standalone setup
        PageableHandlerMethodArgumentResolver pageableResolver = new PageableHandlerMethodArgumentResolver();
        mockMvc = MockMvcBuilders.standaloneSetup(customerRestController)
                .setCustomArgumentResolvers(pageableResolver)
                .build();
    }

    @Test
    void create_shouldReturn201() throws Exception {
        CustomerRequest req = new CustomerRequest();
        req.setName("Thiago");
        req.setSurname("Castoldi");
        // required fields per DTO validation
        req.setEmail("test@example.com");
        req.setDni("12345678");
        req.setPhone("555-1234");

        CustomerResponse resp = new CustomerResponse();
        resp.setCustomerId(1L);
        resp.setName("Thiago");

        when(customerMapper.toEntity(any(CustomerRequest.class))).thenReturn(new Customer());
        when(customerService.create(any(Customer.class))).thenReturn(resp);

        mockMvc.perform(post("/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerId").value(1));

        verify(customerService).create(any(Customer.class));
    }

    @Test
    void getById_shouldReturnFullResponse() throws Exception {
        com.paymentchain.customer.dtos.CustomerFullResponse full = new com.paymentchain.customer.dtos.CustomerFullResponse();
        full.setCustomerId(2L);

        when(customerService.getCustomerWithAccounts(2L)).thenReturn(full);

        mockMvc.perform(get("/customers/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value(2));

        verify(customerService).getCustomerWithAccounts(2L);
    }

    @Test
    void listAll_shouldReturnPage_directCall() throws Exception {
        CustomerResponse resp = new CustomerResponse();
        resp.setCustomerId(3L);

        when(customerService.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(Collections.singletonList(new Customer())));
        when(customerMapper.toResponse(any(Customer.class))).thenReturn(resp);

        // Call controller method directly to avoid MockMvc/Jackson serialization quirks in this unit test
        Pageable pageable = PageRequest.of(0, 10);
        ResponseEntity<org.springframework.data.domain.Page<CustomerResponse>> result = customerRestController.listAll(pageable);

        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getContent()).hasSize(1);
        assertThat(result.getBody().getContent().get(0).getCustomerId()).isEqualTo(3L);

        verify(customerService).findAll(any(Pageable.class));
    }

}
