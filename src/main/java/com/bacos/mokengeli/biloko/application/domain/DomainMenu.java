package com.bacos.mokengeli.biloko.application.domain;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Builder
@Data
public class DomainMenu {
    private Long id;
    private String name;
    private Double price;
    private String tenantCode;
    private List<DomainDish> dishes;
}
