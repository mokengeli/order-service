package com.bacos.mokengeli.biloko.application.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DomainDish {
    private Long id;
    private String name;
    private Double price;
    private DomainCurrency currency;
    private String tenantCode;
    private List<String> categories;
    private List<DomainDishArticle> dishArticles;
}
