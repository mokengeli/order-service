package com.bacos.mokengeli.biloko.application.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DomainDishProduct {

    private DomainDish dish;
    private Long productId;
    private String productName;
    private String unitOfMeasure;
    private Double quantity;
    private Boolean removable;
}

