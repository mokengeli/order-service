package com.bacos.mokengeli.biloko.application.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DomainDishArticle {

    private DomainDish dish;        // Reference to DomainDish
    private DomainArticle article;  // Reference to DomainArticle
    private Double quantity;
    private Boolean removable;
}

