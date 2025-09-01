package com.bacos.mokengeli.biloko.application.port;

import com.bacos.mokengeli.biloko.application.domain.DomainCashierOrderSummary;
import com.bacos.mokengeli.biloko.application.exception.ServiceException;

import java.time.LocalDate;
import java.util.List;

public interface CashierPort {
    DomainCashierOrderSummary getCashierOrderSummary(
            LocalDate date, 
            String searchType, 
            String search,
            String status, 
            String tenantCode
    ) throws ServiceException;

    List<DomainCashierOrderSummary.DomainCashierOrder> getOrdersByTable(
            Long tableId,
            LocalDate date,
            String status,
            String tenantCode
    ) throws ServiceException;
}