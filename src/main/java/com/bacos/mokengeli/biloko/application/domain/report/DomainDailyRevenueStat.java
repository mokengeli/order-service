package com.bacos.mokengeli.biloko.application.domain.report;

import java.time.LocalDate;

public class DomainDailyRevenueStat {
    private final LocalDate date;
    private final long     ordersCount;
    private final double   totalRevenue;
    private final double   averageTicket;
    private final String   currencyCode;

    public DomainDailyRevenueStat(LocalDate date,
                                  long ordersCount,
                                  double totalRevenue,
                                  String currencyCode) {
        this.date          = date;
        this.ordersCount   = ordersCount;
        this.totalRevenue  = totalRevenue;
        this.currencyCode  = currencyCode;
        this.averageTicket = ordersCount > 0
            ? totalRevenue / ordersCount
            : 0.0;
    }

    public LocalDate getDate()         { return date; }
    public long      getOrdersCount()  { return ordersCount; }
    public double    getTotalRevenue() { return totalRevenue; }
    public double    getAverageTicket(){ return averageTicket; }
    public String    getCurrencyCode() { return currencyCode; }
}
