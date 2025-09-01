package com.bacos.mokengeli.biloko.application.service;

import com.bacos.mokengeli.biloko.application.domain.DomainCashierOrderSummary;
import com.bacos.mokengeli.biloko.application.domain.model.ConnectedUser;
import com.bacos.mokengeli.biloko.application.exception.ServiceException;
import com.bacos.mokengeli.biloko.application.port.CashierPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class CashierService {

    private final CashierPort cashierPort;
    private final UserAppService userAppService;

    @Autowired
    public CashierService(CashierPort cashierPort, UserAppService userAppService) {
        this.cashierPort = cashierPort;
        this.userAppService = userAppService;
    }

    public DomainCashierOrderSummary getCashierOrderSummary(
            String tenantCode, LocalDate date,
            String searchType,
            String search,
            String status
    ) throws ServiceException {
        ConnectedUser connectedUser = this.userAppService.getConnectedUser();

        if (!userAppService.isAdminUser() && !connectedUser.getTenantCode().equals(tenantCode)) {
            String uuid = UUID.randomUUID().toString();
            log.error("[{}]: User [{}] Tenant [{}] try to get the get order summary of another tenant: {}", uuid,
                    connectedUser.getEmployeeNumber(), connectedUser.getTenantCode(), tenantCode);
            throw new ServiceException(uuid, "Accès refusé pour ce tenant");
        }
        if (date == null) {
            date = LocalDate.now();
        }

        if (searchType == null || (!searchType.equals("TABLE") && !searchType.equals("ORDER"))) {
            String errorId = UUID.randomUUID().toString();
            log.error("[{}]: User [{}]. Invalid search type: {}", errorId, connectedUser.getEmployeeNumber(), searchType);
            throw new ServiceException(errorId, "Search type must be 'TABLE' or 'ORDER'");
        }

        if (status == null || (!status.equals("ALL") && !status.equals("PAID") && !status.equals("PENDING") && !status.equals("READY"))) {
            String errorId = UUID.randomUUID().toString();
            log.error("[{}]: User [{}]. Invalid status: {}", errorId, connectedUser.getEmployeeNumber(), status);
            throw new ServiceException(errorId, "Status must be 'ALL', 'PAID', 'PENDING', or 'READY'");
        }

        try {
            return cashierPort.getCashierOrderSummary(date, searchType, search, status, tenantCode);
        } catch (ServiceException e) {
            log.error("[{}]: User [{}]. Error getting cashier summary: {}", e.getTechnicalId(), connectedUser.getEmployeeNumber(), e.getMessage());
            throw new ServiceException(e.getTechnicalId(), "An internal error occurred while retrieving cashier summary");
        }
    }

    public List<DomainCashierOrderSummary.DomainCashierOrder> getOrdersByTable(
            String tenantCode,
            Long tableId,
            LocalDate date,
            String status
    ) throws ServiceException {
        ConnectedUser connectedUser = this.userAppService.getConnectedUser();

        if (!userAppService.isAdminUser() &&
                !connectedUser.getTenantCode().equals(tenantCode)) {
            String uuid = UUID.randomUUID().toString();
            log.error("[{}]: User [{}] Tenant [{}] try to get orders for table of another tenant: {}", uuid,
                    connectedUser.getEmployeeNumber(), connectedUser.getTenantCode(), tenantCode);
            throw new ServiceException(uuid, "Accès refusé pour ce tenant");
        }

        if (tableId == null) {
            String errorId = UUID.randomUUID().toString();
            log.error("[{}]: User [{}]. Table ID cannot be null", errorId, connectedUser.getEmployeeNumber());
            throw new ServiceException(errorId, "Table ID is required");
        }

        if (date == null) {
            date = LocalDate.now();
        }

        if (status == null || (!status.equals("ALL") && !status.equals("PAID") && !status.equals("PENDING") && !status.equals("READY"))) {
            String errorId = UUID.randomUUID().toString();
            log.error("[{}]: User [{}]. Invalid status: {}", errorId, connectedUser.getEmployeeNumber(), status);
            throw new ServiceException(errorId, "Status must be 'ALL', 'PAID', 'PENDING', or 'READY'");
        }

        try {
            return cashierPort.getOrdersByTable(tableId, date, status, tenantCode);
        } catch (ServiceException e) {
            log.error("[{}]: User [{}]. Error getting orders by table: {}", e.getTechnicalId(), connectedUser.getEmployeeNumber(), e.getMessage());
            throw new ServiceException(e.getTechnicalId(), "An internal error occurred while retrieving orders by table");
        }
    }
}