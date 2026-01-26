/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.paymentchain.billing.common;

import com.paymentchain.billing.dto.InvoiceRequest;
import com.paymentchain.billing.dto.InvoiceResponse;
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
// componentModel = "spring" permite usar @Autowired InvoiceRequestMapper en el servicio
@Mapper(componentModel = "spring")
public interface InvoiceResponseMapper {
    
    @Mapping(source = "customerId", target = "customer")
    @Mapping(source = "id", target = "invoiceId")
    InvoiceResponse InvoiceToInvoiceResponse(Invoice source);
    
    List<InvoiceResponse> InvoiceListToInvoiceResponseList(List<Invoice> source);
    
    @InheritInverseConfiguration
    InvoiceRequest InvoiceResponseToInvoice(InvoiceResponse source);
    
    @InheritInverseConfiguration
    List<InvoiceRequest> InvoiceResponseListToInvoiceList(List<InvoiceResponse> source);
}
