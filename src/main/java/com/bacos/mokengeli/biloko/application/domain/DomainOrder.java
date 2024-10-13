package com.bacos.mokengeli.biloko.application.domain;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class DomainOrder {
    private Long id;
    private String tenantCode;
    private String employeeNumber;  // Waiter identifier by employee number
    private String tableNumber;
    private List<DomainDish> dishes;
    private List<DomainMenu> menus;  // Menus in the order
    private OrderStatus state;
    private double totalPrice;
    private String comment;  // Optional comment on the order
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
