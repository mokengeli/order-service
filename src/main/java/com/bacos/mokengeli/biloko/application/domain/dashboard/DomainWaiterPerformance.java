package com.bacos.mokengeli.biloko.application.domain.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Performance d'un serveur sur une période donnée.
 */
@Data
@AllArgsConstructor
public class DomainWaiterPerformance {
    private String waiterIdentifier;
    private String waiterName;
    private int ordersCount;
    private double totalRevenue;
    private double averageOrderValue;
    private int totalItemsServed;
}