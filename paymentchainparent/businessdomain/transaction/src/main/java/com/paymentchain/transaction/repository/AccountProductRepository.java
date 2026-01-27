package com.paymentchain.transaction.repository;

import com.paymentchain.transaction.entities.AccountProduct;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountProductRepository extends JpaRepository<AccountProduct, Long> {
}
