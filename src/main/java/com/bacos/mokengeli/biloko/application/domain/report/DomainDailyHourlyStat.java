package com.bacos.mokengeli.biloko.application.domain.report;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class DomainDailyHourlyStat {
    private final LocalDate date;
    private final int      hour;
    private final long     ordersCount;
    private final long     dishesCount;
    private final double   totalRevenue;
    private final String   currencyCode;

}
