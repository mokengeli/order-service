package com.bacos.mokengeli.biloko.application.domain.dashboard;

import com.bacos.mokengeli.biloko.application.domain.DomainCurrency;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * Rapport journalier de synthèse des plats vendus.
 */
@Data
@AllArgsConstructor
public class DomainDailyDishReport {
    private LocalDate date;
    private List<DomainDishSummary> dishSummaries;
    private int totalDishesCount;
    private double totalAmount;
    private DomainCurrency currency;
}