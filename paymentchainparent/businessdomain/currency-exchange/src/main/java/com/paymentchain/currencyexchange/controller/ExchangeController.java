package com.paymentchain.currencyexchange.controller;

import com.paymentchain.currencyexchange.service.CurrencyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

@RestController
@RequestMapping("/exchange")
@RequiredArgsConstructor
@Slf4j
public class ExchangeController {

    private final CurrencyService currencyService;

    @Operation(summary = "Get exchange rate between two currencies", description = "Returns the current exchange rate from `from` to `to`.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Exchange rate returned"),
            @ApiResponse(responseCode = "400", description = "Bad request")
    })
    @GetMapping("/rate")
    public BigDecimal getRate(
            @Parameter(description = "Base currency code", required = true, example = "USD")
            @RequestParam("from") String from,
            @Parameter(description = "Target currency code", required = true, example = "ARS")
            @RequestParam("to") String to,
            HttpServletRequest request
    ) {
        // Log incoming request source and raw params
        String remote = request.getRemoteAddr();
        log.info("[HTTP] /exchange/rate called from {} with from='{}', to='{}'", remote, from, to);

        if (from == null || to == null) {
            log.error("Invalid /exchange/rate call: null parameters from={} to={} fromIp={}", from, to, remote);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Query parameters 'from' and 'to' are required");
        }
        String fromSan = from.trim();
        String toSan = to.trim();
        if (fromSan.isEmpty() || toSan.isEmpty() || "null".equalsIgnoreCase(fromSan) || "null".equalsIgnoreCase(toSan)) {
            log.error("Invalid /exchange/rate call: invalid parameter values from='{}' to='{}' fromIp={}", from, to, remote);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Query parameters 'from' and 'to' must be non-empty and not 'null'");
        }

        return currencyService.getExchangeRate(fromSan, toSan);
    }
}