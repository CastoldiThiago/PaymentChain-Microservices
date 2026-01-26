/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.paymentchain.billing.common;

import com.paymentchain.billing.dto.InvoiceRequest;
import com.paymentchain.billing.entities.Invoice;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.List;
import org.mapstruct.InheritInverseConfiguration;

/**
 *
 * @author casto
 */
// 2. ANOTACIÓN OBLIGATORIA
// componentModel = "spring" permite usar @Autowired InvoiceRequestMapper en tu servicio
@Mapper(componentModel = "spring")
public interface InvoiceRequestMapper {
    
    @Mapping(source = "customer", target = "customerId")
    @Mapping(target = "id", ignore = true)
    Invoice InvoiceRequestToInvoice(InvoiceRequest source);
    
    List<Invoice> InvoiceRequestListToInvoiceList(List<InvoiceRequest> source);
    @InheritInverseConfiguration
    InvoiceRequest InvoiceToInvoiceRequest(Invoice source);
    
    @InheritInverseConfiguration
    List<InvoiceRequest> InvoiceListToInvoiceRequestList(List<Invoice> source);
}
