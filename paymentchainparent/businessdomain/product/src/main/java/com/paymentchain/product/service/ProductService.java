/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.paymentchain.product.service;

import com.paymentchain.product.entities.Product;
import com.paymentchain.product.exception.BusinessRuleException;
import com.paymentchain.product.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 *
 * @author casto
 */
@Service
public class ProductService {
    @Autowired
    ProductRepository productRepository;
    
    public Product save(Product input) throws BusinessRuleException{
        if ( (input.getCode().isBlank()) || (input.getName().isBlank()) ){
            BusinessRuleException businessRuleException = new BusinessRuleException("1010", "Error validacion de guardado, código y nombre de producto son obligatorios", HttpStatus.PRECONDITION_FAILED);
                   throw businessRuleException;
        }
        else {
            Product save = productRepository.save(input);
            return save;
        }
    }
}
