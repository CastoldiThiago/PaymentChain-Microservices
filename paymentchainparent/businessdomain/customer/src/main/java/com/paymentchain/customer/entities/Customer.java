/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.paymentchain.customer.entities;

import jakarta.persistence.*;
import lombok.Data;
/**
 *
 * @author casto
 */
@Entity
@Data
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String surname;
    private String phone;

    @Column(unique = true) // El DNI no se repite
    private String dni;

    @Column(unique = true) // El mail tampoco
    private String email;

    private String status; // "CREATED", "ACTIVE", "BLOCKED"

}
