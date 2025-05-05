package com.bacos.mokengeli.biloko.application.domain.dashboard;


import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Ventilation CA/volume par catégorie pour le dashboard.
 */
@Data
@AllArgsConstructor
public class DomainCategoryBreakdown {
    private String categoryName;
    private Long value;
    private Double revenue;

}

