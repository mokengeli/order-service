package com.bacos.mokengeli.biloko.application.domain;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class DomainDish {
    private Long id;
    private String tenantCode;  // New field for multi-tenant support
    private String name;
    private BigDecimal price;
    private List<DomainArticle> articles;  // List of articles (ingredients) with quantity and unit of measure
}
