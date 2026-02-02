package com.paymentchain.transaction.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paymentchain.transaction.dtos.AccountResponse;
import com.paymentchain.transaction.dtos.CreateAccountRequest;
import com.paymentchain.transaction.entities.Account;
import com.paymentchain.transaction.mapper.AccountMapper;
import com.paymentchain.transaction.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AccountControllerUnitTest {

    private MockMvc mockMvc;

    @Mock
    private AccountService accountService;

    @Mock
    private AccountMapper accountMapper;

    @InjectMocks
    private com.paymentchain.transaction.controller.AccountController accountController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(accountController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    void getByIban_shouldReturn200() throws Exception {
        Account a = new Account();
        a.setIban("AR-GET-1");
        a.setBalance(new java.math.BigDecimal("100"));

        AccountResponse resp = new AccountResponse();
        resp.setIban("AR-GET-1");
        resp.setBalance(a.getBalance());

        when(accountService.findByIban("AR-GET-1")).thenReturn(a);
        when(accountMapper.toResponse(a)).thenReturn(resp);

        mockMvc.perform(get("/accounts/AR-GET-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.iban").value("AR-GET-1"));
    }

    @Test
    void postCreate_shouldReturn201() throws Exception {
        CreateAccountRequest req = new CreateAccountRequest();
        req.setIban("AR-NEW-1");
        req.setBalance(new java.math.BigDecimal("10"));
        req.setCustomerId(1L);
        req.setCurrency("ARS");
        req.setProductId(1L);

        Account saved = new Account();
        saved.setIban("AR-NEW-1");
        saved.setBalance(req.getBalance());

        when(accountService.create(any(CreateAccountRequest.class))).thenReturn(saved);
        when(accountMapper.toResponse(saved)).thenReturn(new AccountResponse());

        mockMvc.perform(post("/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }
}
