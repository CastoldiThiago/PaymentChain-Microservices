package com.paymentchain.transaction;

import com.paymentchain.transaction.entities.Account;
import com.paymentchain.transaction.entities.AccountProduct;
import com.paymentchain.transaction.repository.AccountProductRepository;
import com.paymentchain.transaction.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class DataSetup implements CommandLineRunner {

    private final AccountProductRepository productRepository;
    private final AccountRepository accountRepository;

    @Override
    public void run(String... args) throws Exception {
        // Validamos si ya hay datos para no duplicarlos al reiniciar
        if (productRepository.count() == 0) {

            System.out.println("Inicializando datos de prueba...");

            // 1. Crear Producto: CAJA DE AHORRO (Sin comisión)
            AccountProduct savings = new AccountProduct();
            savings.setName("Caja de Ahorro");
            savings.setTransactionFeePercentage(BigDecimal.ZERO); // 0%
            // Guardamos y recuperamos para tener el ID
            savings = productRepository.save(savings);

            // 2. Crear Producto: CUENTA GOLD (Comisión 1%)
            AccountProduct gold = new AccountProduct();
            gold.setName("Cuenta Corriente Gold");
            gold.setTransactionFeePercentage(new BigDecimal("0.01")); // 1%
            gold = productRepository.save(gold);

            // 3. Crear una Cuenta de Prueba Lista para usar
            Account testAccount = new Account();
            testAccount.setIban("AR0001"); // IBAN conocido para probar en Postman
            testAccount.setBalance(new BigDecimal("10000")); // Saldo inicial generoso
            testAccount.setCustomerId(1L); // ID ficticio de cliente
            testAccount.setProduct(gold); // Le asignamos el producto que cobra comisión

            accountRepository.save(testAccount);

            System.out.println("Datos cargados: Cuenta AR0001 con $10.000 y Configuración Gold (1%)");
        }
    }
}
