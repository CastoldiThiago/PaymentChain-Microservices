/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.paymentchain.customer.businnes.transactions;

import com.fasterxml.jackson.databind.JsonNode;
import com.paymentchain.customer.entities.Customer;
import com.paymentchain.customer.entities.CustomerProduct;
import com.paymentchain.customer.exception.BusinessRuleException;
import com.paymentchain.customer.repository.CustomerRepository;
import io.netty.channel.ChannelOption;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import jakarta.ws.rs.NotFoundException;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.netty.http.client.HttpClient;

/**
 *
 * @author casto
 */
@Service
public class BusinessTransaction {
    
    @Autowired
    CustomerRepository customerRepository;
    
    @Autowired
    private WebClient.Builder webClientBuilder;
    
    /*private final WebClient.Builder webClientBuilder;
    
    // Inicialización de web client
    public CustomerRestController(WebClient.Builder webClientBuilder){
        this.webClientBuilder = webClientBuilder;
    }*/
    
    // webClient requires HTTPClient library to work propertly
    HttpClient client = HttpClient.create()
            // Connection timeout
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
            .option(ChannelOption.SO_KEEPALIVE, true)
            .option(EpollChannelOption.TCP_KEEPIDLE, 300)
            .option(EpollChannelOption.TCP_KEEPINTVL, 60)
            // Response Timeout
            .responseTimeout(Duration.ofSeconds(1))
            .doOnConnected(connection -> {
            connection.addHandlerLast(new ReadTimeoutHandler(5000, TimeUnit.MILLISECONDS));
            connection.addHandlerLast(new WriteTimeoutHandler(5000, TimeUnit.MILLISECONDS));
            });
    
    /**
     * Call Product Microservice , find a product by Id and return it name
     *
     * @param id of product to find
     * @return name of product if it was find
     */
    private String getProductName(long id) throws UnknownHostException{
        String name = "";
        try {
        
        WebClient build = webClientBuilder
                .clientConnector(new ReactorClientHttpConnector(client))
                .baseUrl("http://BUSINESSDOMAIN-PRODUCT/product")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultUriVariables(Collections.singletonMap("url", "http://BUSINESSDOMAIN-PRODUCT/product"))
                .build();
        
        JsonNode block = build.method(HttpMethod.GET).uri("/" + id)
                   .retrieve().bodyToMono(JsonNode.class).block();
        
        name = block.get("name").asText();
        }catch(WebClientResponseException ex){
            if(ex.getStatusCode() == HttpStatus.NOT_FOUND){
                return "";
            }else{
                throw new UnknownHostException(ex.getMessage());
            }
        }  
        return name;
    }
    
    /**
     * Call Transaction Microservice and Find all transaction that belong to the
     * account give
     *
     * @param iban account number of the customer
     * @return All transaction that belong this account
     */
    private List<?> getTransactions(long customerId){
        
        WebClient build = webClientBuilder
                .clientConnector(new ReactorClientHttpConnector(client))
                .baseUrl("http://BUSINESSDOMAIN-TRANSACTION/transaction")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultUriVariables(Collections.singletonMap("url", "http://BUSINESSDOMAIN-TRANSACTION/transaction"))
                .build();
        
        List<?> transactions = build.method(HttpMethod.GET).uri(uriBuilder -> uriBuilder
        .path("/customer/transactions")
        .queryParam("customerId", customerId)
        .build())
         .retrieve().bodyToFlux(Object.class).collectList().block();
        
        return transactions;
    }
    
    public Customer post(@RequestBody Customer input) throws BusinessRuleException,UnknownHostException {
        
       if (input.getProducts() != null){
           for (Iterator<CustomerProduct> it = input.getProducts().iterator(); it.hasNext();) {
               CustomerProduct dto = it.next();
               String productName = getProductName(dto.getProductId());
               if(productName.isBlank()){
                   BusinessRuleException businessRuleException = new BusinessRuleException("1025", "Error validacion, producto con id "+dto.getProductId()+" no existe", HttpStatus.PRECONDITION_FAILED);
                   throw businessRuleException;
               }else {
                   dto.setCustomer(input);
               }
           }
       }
        
        Customer save = customerRepository.save(input);
        return save;
    }
    
    public Customer get(@RequestParam(name = "id") Long id) throws UnknownHostException {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Customer no encontrado"));
        if (customer != null){
            List<CustomerProduct> products = customer.getProducts();

            // For each product find it name
            for (CustomerProduct x : products) {
                String productName = getProductName(x.getProductId());
                x.setProductName(productName);
            }

            //find all transactions that belong this account number
            List<?> transactions = getTransactions(customer.getId());
            customer.setTransactions(transactions);
        }
        return customer;
    }
}
