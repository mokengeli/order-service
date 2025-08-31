package com.bacos.mokengeli.biloko.application.domain.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * Résumé d'un plat dans le rapport journalier.
 */
@Data
@AllArgsConstructor
public class DomainDishSummary {
    private Long dishId;
    private String dishName;
    private int quantityServed;
    private double unitPrice;
    private double totalAmount;
    private List<String> categories;
}