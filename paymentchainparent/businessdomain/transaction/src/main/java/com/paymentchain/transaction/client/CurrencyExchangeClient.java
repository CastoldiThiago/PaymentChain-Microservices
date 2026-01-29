package com.paymentchain.transaction.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@FeignClient(name = "currency-exchange", url = "${CURRENCY_SERVICE_URL:http://localhost:8090}")
public interface CurrencyExchangeClient {

    @GetMapping("/exchange/rate")
    BigDecimal getExchangeRate(
            @RequestParam("from") String from,
            @RequestParam("to") String to);
}
