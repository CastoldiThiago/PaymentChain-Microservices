package com.paymentchain.customer.service;

import com.paymentchain.customer.entities.Customer;
import com.paymentchain.customer.exception.DuplicateResourceException;
import com.paymentchain.customer.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceUnitTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void createCustomer_shouldThrowWhenEmailExists() {
        Customer incoming = new Customer();
        incoming.setEmail("existing@example.com");

        when(customerRepository.existsByEmail("existing@example.com")).thenReturn(true);

        DuplicateResourceException ex = assertThrows(DuplicateResourceException.class,
                () -> customerService.create(incoming));

        assertThat(ex.getField()).isEqualTo("email");
        verify(customerRepository, never()).save(any());
    }

    @Test
    void createCustomer_shouldThrowWhenDniExists() {
        Customer incoming = new Customer();
        incoming.setDni("12345678");

        // only stub what's necessary for this test
        when(customerRepository.existsByDni("12345678")).thenReturn(true);

        DuplicateResourceException ex = assertThrows(DuplicateResourceException.class,
                () -> customerService.create(incoming));

        assertThat(ex.getField()).isEqualTo("dni");
        verify(customerRepository, never()).save(any());
    }

    @Test
    void update_shouldModifyAndSaveExistingCustomer() {
        Customer existing = new Customer();
        existing.setId(1L);
        existing.setName("Old");
        existing.setSurname("Name");
        existing.setPhone("000");
        existing.setDni("111");
        existing.setEmail("old@example.com");

        Customer updated = new Customer();
        updated.setName("New");
        updated.setSurname("Person");
        updated.setPhone("999");
        updated.setDni("222");
        updated.setEmail("new@example.com");

        when(customerRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));

        Customer result = customerService.update(1L, updated);

        assertThat(result.getName()).isEqualTo("New");
        assertThat(result.getSurname()).isEqualTo("Person");
        assertThat(result.getPhone()).isEqualTo("999");
        assertThat(result.getDni()).isEqualTo("222");
        assertThat(result.getEmail()).isEqualTo("new@example.com");

        verify(customerRepository).save(existing);
    }

    @Test
    void delete_shouldThrowWhenNotExists() {
        when(customerRepository.existsById(10L)).thenReturn(false);
        RuntimeException ex = assertThrows(RuntimeException.class, () -> customerService.delete(10L));
        assertThat(ex.getMessage()).contains("Customer not found");
    }

    @Test
    void delete_shouldRemoveWhenExists() {
        when(customerRepository.existsById(2L)).thenReturn(true);
        doNothing().when(customerRepository).deleteById(2L);

        customerService.delete(2L);

        verify(customerRepository).deleteById(2L);
    }

    @Test
    void findAll_withPageable_shouldReturnPage() {
        Customer c = new Customer();
        c.setId(5L);
        Page<Customer> page = new PageImpl<>(Collections.singletonList(c));
        Pageable pageable = PageRequest.of(0, 10);

        when(customerRepository.findAll(pageable)).thenReturn(page);

        Page<Customer> result = customerService.findAll(pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(5L);
    }

}
