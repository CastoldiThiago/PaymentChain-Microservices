package com.paymentchain.currencyexchange.controller;

import com.paymentchain.currencyexchange.dtos.ExchangeRateResponse;
import com.paymentchain.currencyexchange.service.CurrencyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/exchange")
@RequiredArgsConstructor
public class ExchangeController {

    private final CurrencyService currencyService;

    @Operation(summary = "Get exchange rate between two currencies", description = "Returns the current exchange rate from `from` to `to`.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Exchange rate returned"),
            @ApiResponse(responseCode = "400", description = "Bad request")
    })
    @GetMapping("/rate")
    public ExchangeRateResponse getRate(
            @Parameter(description = "Base currency code", required = true, example = "USD")
            @RequestParam("from") String from,
            @Parameter(description = "Target currency code", required = true, example = "ARS")
            @RequestParam("to") String to
    ) {
        BigDecimal rate = currencyService.getExchangeRate(from, to);
        ExchangeRateResponse resp = new ExchangeRateResponse();
        resp.setFrom(from);
        resp.setTo(to);
        resp.setRate(rate);
        return resp;
    }
}