package com.paymentchain.transaction.mapper;

import com.paymentchain.transaction.dtos.TransactionResponse;
import com.paymentchain.transaction.entities.Transaction;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class TransactionMapper {

    public TransactionResponse toResponse(Transaction entity) {
        TransactionResponse dto = new TransactionResponse();
        dto.setTransactionId(entity.getId());
        // Validamos null por seguridad
        if (entity.getAccount() != null) {
            dto.setAccountIban(entity.getAccount().getIban());
        }
        dto.setAmount(entity.getAmount());
        dto.setFee(entity.getFee());
        dto.setTotal(entity.getTotal());
        dto.setDate(entity.getDate());
        dto.setReference(entity.getReference());
        dto.setStatus(entity.getStatus());
        dto.setCurrency(entity.getCurrency());
        return dto;
    }

    public List<TransactionResponse> toResponseList(List<Transaction> entities) {
        return entities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
