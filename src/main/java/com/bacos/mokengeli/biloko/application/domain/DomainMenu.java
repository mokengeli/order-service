package com.bacos.mokengeli.biloko.application.domain;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class DomainMenu {
    private Long id;
    private String name;
    private Double price;
    private DomainTenantContext tenantContext;
    private List<DomainDish> dishes;
}
