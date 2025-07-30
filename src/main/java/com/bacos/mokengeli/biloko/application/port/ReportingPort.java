package com.bacos.mokengeli.biloko.application.port;

import com.bacos.mokengeli.biloko.application.domain.report.DomainDailyRevenueStat;

import java.time.LocalDate;
import java.util.List;

public interface ReportingPort {
    /**
     * Calcule pour chaque jour de [startDate,endDate] :
     * - le nombre de commandes,
     * - le CA total (somme des order.getTotalPrice()),
     * - le ticket moyen,
     * - la devise associée.
     */
    List<DomainDailyRevenueStat> getDailyRevenueStats(
        LocalDate startDate,
        LocalDate endDate,
        String tenantCode
    );
}