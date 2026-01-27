package com.paymentchain.transaction.mapper;

import com.paymentchain.transaction.dtos.AccountResponse;
import com.paymentchain.transaction.entities.Account;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AccountMapper {

    public AccountResponse toResponse(Account entity) {
        if (entity == null) {
            return null;
        }

        AccountResponse dto = new AccountResponse();
        dto.setAccountId(entity.getId());
        dto.setIban(entity.getIban());
        dto.setBalance(entity.getBalance());
        dto.setCustomerId(entity.getCustomerId());

        // Mapeamos datos del producto si existe
        if (entity.getProduct() != null) {
            dto.setProductName(entity.getProduct().getName());
            dto.setTransactionFee(entity.getProduct().getTransactionFeePercentage());
        }

        return dto;
    }

    public List<AccountResponse> toResponseList(List<Account> entities) {
        return entities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
