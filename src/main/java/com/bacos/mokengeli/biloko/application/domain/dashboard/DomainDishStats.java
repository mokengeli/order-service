package com.bacos.mokengeli.biloko.application.domain.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * Statistiques globales et découpées des plats servis.
 */
@Data
@AllArgsConstructor
public class DomainDishStats {
    private long totalDishesServed;
    private List<DomainDishCategoryStat> dishesPerCategory;
    private List<DomainDishHourStat> dishesPerHour;
}