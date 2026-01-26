/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.paymentchain.billing.controller;

import com.paymentchain.billing.common.InvoiceRequestMapper;
import com.paymentchain.billing.common.InvoiceResponseMapper;
import com.paymentchain.billing.dto.InvoiceRequest;
import com.paymentchain.billing.dto.InvoiceResponse;
import com.paymentchain.billing.entities.Invoice;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import com.paymentchain.billing.respository.InvoiceRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Optional;
import org.springframework.http.HttpStatus;

/**
 * REST controller para gestión de facturas (Invoices).
 * <p>
 * Exponemos endpoints CRUD que aceptan y devuelven DTOs (InvoiceRequest / InvoiceResponse).
 * Los mapeos entre entidad y DTO se delegan a los mappers inyectados.
 */
@Tag(name = "Billing API", description = "This API serve all functionality for management Invoices")
@RestController
@RequestMapping("/billing")
public class InvoiceRestController {
    
    @Autowired
    InvoiceRepository billingRepository;
    @Autowired
    InvoiceResponseMapper invoiceResponseMapper;
    @Autowired
    InvoiceRequestMapper invoiceRequestMapper;
    
    /**
     * Obtiene todas las facturas y las transforma a DTOs.
     * Devuelve 204 No Content si no hay facturas.
     */
    @Operation(description = "Return all invoices bundled into response", summary = "Return 204 if no data found")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Exito"),
        @ApiResponse(responseCode = "500", description = "Internal Error")
    })
    @GetMapping()
    public ResponseEntity<List<InvoiceResponse>> list() {
        List<Invoice> findAll = billingRepository.findAll();
        List<InvoiceResponse> responses = invoiceResponseMapper.InvoiceListToInvoiceResponseList(findAll);
        if (responses == null || responses.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(responses);
    }
    
    /**
     * Obtiene una factura por su id y devuelve su DTO.
     * @param id identificador de la factura
     * @return 200 con InvoiceResponse o 404 si no existe
     */
    @GetMapping("/{id}")
    public ResponseEntity<InvoiceResponse>  get(@PathVariable(name = "id") long id) {
        Optional<Invoice> invoice = billingRepository.findById(id);
        if (invoice.isPresent()) {
            InvoiceResponse dto = invoiceResponseMapper.InvoiceToInvoiceResponse(invoice.get());
            return new ResponseEntity<>(dto, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    /**
     * Actualiza una factura existente con los datos recibidos en el DTO de petición.
     * Si la factura no existe devuelve 404.
     * @param id id de la factura a actualizar
     * @param input datos de actualización (InvoiceRequest)
     * @return InvoiceResponse actualizado
     */
    @PutMapping("/{id}")
    public ResponseEntity<InvoiceResponse> put(@PathVariable(name = "id") long id, @RequestBody InvoiceRequest input) {
        Optional<Invoice> findById = billingRepository.findById(id);
        if(findById.isEmpty()){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        // Mapear DTO a entidad y asegurarse de mantener el id
        Invoice invoiceRequestToInvoice = invoiceRequestMapper.InvoiceRequestToInvoice(input);
        invoiceRequestToInvoice.setId(id);
        Invoice save = billingRepository.save(invoiceRequestToInvoice);
        InvoiceResponse invoiceToInvoiceResponse = invoiceResponseMapper.InvoiceToInvoiceResponse(save);
        return ResponseEntity.ok(invoiceToInvoiceResponse);

    }
    
    /**
     * Crea una nueva factura a partir del DTO de petición y devuelve el DTO de respuesta.
     */
    @PostMapping
    public ResponseEntity<InvoiceResponse> post(@RequestBody InvoiceRequest input) {
        Invoice invoice = invoiceRequestMapper.InvoiceRequestToInvoice(input);
        Invoice save = billingRepository.save(invoice);
        InvoiceResponse invoiceResponse = invoiceResponseMapper.InvoiceToInvoiceResponse(save);
        return ResponseEntity.ok(invoiceResponse);
    }
    
    /**
     * Elimina una factura y devuelve el DTO de la factura eliminada.
     * @param id id de la factura a eliminar
     * @return 200 con InvoiceResponse del recurso eliminado, o 404 si no existe
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<InvoiceResponse> delete(@PathVariable(name = "id") long id) {
        Optional<Invoice> dto = billingRepository.findById(id);
        if (dto.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Invoice entity = dto.get();
        InvoiceResponse response = invoiceResponseMapper.InvoiceToInvoiceResponse(entity);
        billingRepository.delete(entity);
        return ResponseEntity.ok(response);
    }
    
}
