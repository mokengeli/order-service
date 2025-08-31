package com.bacos.mokengeli.biloko.application.port;

import com.bacos.mokengeli.biloko.application.domain.DomainOrder;
import com.bacos.mokengeli.biloko.application.domain.dashboard.*;

import java.time.LocalDate;
import java.util.List;

public interface DashboardPort {
    List<DomainOrder> getOrdersBetweenDates(LocalDate startDate, LocalDate endDate, String tenantCode);

    List<DomainTopDish> getTopDishesServed(
            LocalDate startDate,
            LocalDate endDate,
            String tenantCode,
            int limit
    );

    List<DomainCategoryBreakdown> getBreakdownByCategory(
            LocalDate startDate,
            LocalDate endDate,
            String tenantCode
    );

    DomainDishStats getDishStats(
            LocalDate startDate,
            LocalDate endDate,
            String tenantCode
    );

    List<DomainHourlyOrderStat> getHourlyOrderDistribution(
            LocalDate date,
            String tenantCode
    );

    List<DomainDailyOrderStat> getDailyOrderDistribution(
            LocalDate startDate,
            LocalDate endDate,
            String tenantCode
    );

    List<DomainDailyDishStat> getDailyDishDistribution(
            LocalDate start,
            LocalDate end,
            String tenantCode);

    List<DomainHourlyDishStat> getHourlyDishDistribution(
            LocalDate date,
            String tenantCode
    );

    List<DomainPaymentStatusStat> getOrderCountByPaymentStatus(
            LocalDate startDate,
            LocalDate endDate,
            String tenantCode
    );

    DomainDailyDishReport getDailyDishReport(
            LocalDate date,
            String tenantCode
    );
}
