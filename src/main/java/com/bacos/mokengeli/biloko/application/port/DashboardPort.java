package com.bacos.mokengeli.biloko.application.port;

import com.bacos.mokengeli.biloko.application.domain.DomainOrder;

import java.time.LocalDate;
import java.util.List;

public interface DashboardPort {
    List<DomainOrder> getOrdersBetweenDates(LocalDate startDate, LocalDate endDate, String tenantCode);
}
