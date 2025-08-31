package com.bacos.mokengeli.biloko.application.domain.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * Rapport de performance des serveurs sur une période.
 */
@Data
@AllArgsConstructor
public class DomainWaiterPerformanceReport {
    private LocalDate startDate;
    private LocalDate endDate;
    private List<DomainWaiterPerformance> waiterStats;
    private int totalOrders;
    private double totalRevenue;
}