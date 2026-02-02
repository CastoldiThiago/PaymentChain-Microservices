package com.paymentchain.transaction.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paymentchain.transaction.dtos.CreateTransactionRequest;
import com.paymentchain.transaction.dtos.TransactionResponse;
import com.paymentchain.transaction.enums.TransactionType;
import com.paymentchain.transaction.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TransactionControllerUnitTest {

    private MockMvc mockMvc;

    @Mock
    private TransactionService transactionService;

    private ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        TransactionRestController controller = new TransactionRestController(transactionService, null);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    void postDeposit_shouldReturn201() throws Exception {
        CreateTransactionRequest req = new CreateTransactionRequest();
        req.setAccountIban("AR-DEP-UNIT");
        req.setAmount(new BigDecimal("100"));
        req.setCurrency("ARS");
        req.setReference("REF_UNIT_POST");
        req.setType(TransactionType.DEPOSIT);

        when(transactionService.performTransaction(any(CreateTransactionRequest.class))).thenReturn(new TransactionResponse());

        mockMvc.perform(post("/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }
}
