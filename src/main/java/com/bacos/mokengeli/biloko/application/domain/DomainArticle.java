package com.bacos.mokengeli.biloko.application.domain;

import lombok.Data;

@Data
public class DomainArticle {
    private Long id;
    private String name;
    private double quantity;  // Quantity of the article used
    private String unitOfMeasure;  // Unit of measure for the article (e.g., "kg", "L")
}
