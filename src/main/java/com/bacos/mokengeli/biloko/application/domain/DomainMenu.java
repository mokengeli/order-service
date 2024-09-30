package com.bacos.mokengeli.biloko.application.domain;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class DomainMenu {
    private Long id;
    private String tenantCode;  // New field for multi-tenant support
    private String name;
    private List<DomainDish> dishes;
    private BigDecimal price;  // Custom menu price, separate from the sum of individual dishes
}
