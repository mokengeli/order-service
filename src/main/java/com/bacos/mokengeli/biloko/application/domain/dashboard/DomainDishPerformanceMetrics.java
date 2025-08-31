package com.bacos.mokengeli.biloko.application.domain.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

/**
 * Métriques de performance des plats pour une date donnée.
 */
@Data
@AllArgsConstructor
public class DomainDishPerformanceMetrics {
    private LocalDate date;
    
    // KPI 2: Métriques de Performance
    private double averageQuantityPerDish;
    private double averageRevenuePerDish;
    private double averageTicketPrice;
    private int uniqueDishesCount;
    private double totalRevenue;
    private int totalQuantity;
    
    // KPI 6: Ratios de Performance
    private double topDishRevenueRatio;        // CA du meilleur plat vs moyenne
    private double concentrationRatio;         // % CA des 20% meilleurs plats
    private String topDishName;               // Nom du plat le plus performant
    private double topDishRevenue;            // CA du plat le plus performant
}