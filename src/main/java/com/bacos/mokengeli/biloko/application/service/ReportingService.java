// src/main/java/com/yourcompany/service/ReportingService.java
package com.bacos.mokengeli.biloko.application.service;


import com.bacos.mokengeli.biloko.application.domain.model.ConnectedUser;
import com.bacos.mokengeli.biloko.application.domain.report.DomainDailyHourlyStat;
import com.bacos.mokengeli.biloko.application.domain.report.DomainDailyRevenueStat;
import com.bacos.mokengeli.biloko.application.exception.ServiceException;
import com.bacos.mokengeli.biloko.application.port.ReportingPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class ReportingService {

    private final ReportingPort reportingPort;
    private final UserAppService userAppService;

    @Autowired
    public ReportingService(ReportingPort reportingPort, UserAppService userAppService) {
        this.reportingPort = reportingPort;
        this.userAppService = userAppService;
    }

    public List<DomainDailyRevenueStat> fetchDailyRevenue(
            LocalDate start,
            LocalDate end,
            String tenantCode
    ) throws ServiceException {
        ConnectedUser connectedUser = userAppService.getConnectedUser();
        if (!userAppService.isAdminUser() &&
                !connectedUser.getTenantCode().equals(tenantCode)) {
            String uuid = UUID.randomUUID().toString();
            log.error("[{}]: User [{}] Tenant [{}] try to get the fetchDailyRevenue of another tenant: {}", uuid,
                    connectedUser.getEmployeeNumber(), connectedUser.getTenantCode(), tenantCode);
            throw new ServiceException(uuid, "Accès refusé pour ce tenant");
        }
        return reportingPort.getDailyRevenueStats(start, end, tenantCode);
    }

    public List<DomainDailyHourlyStat> fetchDailyHourlyMatrix(
            LocalDate start,
            LocalDate end,
            String tenantCode
    ) throws ServiceException {
        ConnectedUser connectedUser = userAppService.getConnectedUser();
        if (!userAppService.isAdminUser() &&
                !connectedUser.getTenantCode().equals(tenantCode)) {
            String uuid = UUID.randomUUID().toString();
            log.error("[{}]: User [{}] Tenant [{}] try to get the fetchDailyHourlyMatrix of another tenant: {}", uuid,
                    connectedUser.getEmployeeNumber(), connectedUser.getTenantCode(), tenantCode);
            throw new ServiceException(uuid, "Accès refusé pour ce tenant");
        }
        return reportingPort.getDailyHourlyMatrix(start, end, tenantCode);
    }

}
