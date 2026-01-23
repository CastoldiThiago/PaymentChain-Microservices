/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.paymentchain.customer.entities;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Transient;
import java.util.List;
import lombok.Data;
/**
 *
 * @author casto
 */
@Data
@Entity
public class Customer {
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    private long id;
    private String dni;
    private String name;
    private String phone;
    private String surname;
    private String address;
    
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "customer" , cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CustomerProduct> products;
    @Transient
    private List<?> transactions;
}
