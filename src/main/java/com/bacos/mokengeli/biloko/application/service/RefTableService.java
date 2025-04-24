package com.bacos.mokengeli.biloko.application.service;


import com.bacos.mokengeli.biloko.application.domain.DomainCategory;
import com.bacos.mokengeli.biloko.application.domain.DomainRefTable;
import com.bacos.mokengeli.biloko.application.domain.model.ConnectedUser;
import com.bacos.mokengeli.biloko.application.exception.ServiceException;
import com.bacos.mokengeli.biloko.application.port.OrderPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class RefTableService {

    private final UserAppService userAppService;
    private final OrderPort orderPort;

    public RefTableService(UserAppService userAppService, OrderPort orderPort) {
        this.userAppService = userAppService;
        this.orderPort = orderPort;
    }

    public Page<DomainRefTable> getRefTablesByTenantCode(String tenantCode, int page, int size) throws ServiceException {
        ConnectedUser connectedUser = this.userAppService.getConnectedUser();
        String connectedUserTenantCode = connectedUser.getTenantCode();
        if (!this.userAppService.isAdminUser()
                && !connectedUserTenantCode.equals(tenantCode)) {
            String uuid = UUID.randomUUID().toString();
            log.error("[{}]: User [{}] user tenant {} try to get a table  of another tenant {}", uuid,
                    connectedUser.getEmployeeNumber(), connectedUser.getTenantCode(), tenantCode);

            throw new ServiceException(uuid, "You don't have permission to retreive the table of another partener");
        }
        return orderPort.getRefTablesByTenantCode(tenantCode, page, size);
    }

    public DomainRefTable createRefTable(DomainRefTable refTable) throws ServiceException {
        ConnectedUser connectedUser = this.userAppService.getConnectedUser();
        String connectedUserTenantCode = connectedUser.getTenantCode();
        String tenantCode = refTable.getTenantCode();

        if (!this.userAppService.isAdminUser()
                && !connectedUserTenantCode.equals(tenantCode)) {
            String uuid = UUID.randomUUID().toString();
            log.error("[{}]: User [{}] user tenant {} try to create a table for another tenant {}", uuid,
                    connectedUser.getEmployeeNumber(), connectedUser.getTenantCode(), tenantCode);

            throw new ServiceException(uuid, "You don't have permission to create table for antother partener");
        }
        try {
            return this.orderPort.createRefTable(refTable);
        } catch (ServiceException e) {
            log.error("[{}]: User [{}]. message: {}", e.getTechnicalId(),
                    connectedUser.getEmployeeNumber(), e.getMessage());
            throw new ServiceException(e.getTechnicalId(), "An internal error occurred while creating the table");
        }
    }
}
