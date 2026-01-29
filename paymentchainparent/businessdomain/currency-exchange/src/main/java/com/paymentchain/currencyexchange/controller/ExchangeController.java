package com.paymentchain.currencyexchange.controller;

import com.paymentchain.currencyexchange.service.CurrencyService;
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

    @GetMapping("/rate")
    public BigDecimal getRate(
            @RequestParam("from") String from,  // <--- AGREGAR ("from")
            @RequestParam("to") String to       // <--- AGREGAR ("to")
    ) {
        return currencyService.getExchangeRate(from, to);
    }
}