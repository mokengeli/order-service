package com.bacos.mokengeli.biloko.application.service;


import com.bacos.mokengeli.biloko.application.domain.DomainRefTable;
import com.bacos.mokengeli.biloko.application.domain.model.ConnectedUser;
import com.bacos.mokengeli.biloko.application.exception.ServiceException;
import com.bacos.mokengeli.biloko.application.port.OrderPort;
import com.bacos.mokengeli.biloko.application.port.RefTablePort;
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
    private final RefTablePort refTablePort;

    public RefTableService(UserAppService userAppService, OrderPort orderPort, RefTablePort refTablePort) {
        this.userAppService = userAppService;
        this.orderPort = orderPort;
        this.refTablePort = refTablePort;
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

    public long countRefTablesByTenantCode(String tenantCode) throws ServiceException {
        ConnectedUser connectedUser = userAppService.getConnectedUser();
        if (!userAppService.isAdminUser() && !connectedUser.getTenantCode().equals(tenantCode)) {
            String uuid = UUID.randomUUID().toString();
            log.error("[{}]: User [{}] user tenant {} try to get table count of another tenant {}", uuid,
                    connectedUser.getEmployeeNumber(), connectedUser.getTenantCode(), tenantCode);

            throw new ServiceException(uuid, "You don't have permission to get the count table of another partener");
        }
        return refTablePort.countRefTablesByTenantCode(tenantCode);
    }

    public DomainRefTable getRefTableById(Long id, String tenantCode) throws ServiceException {
        ConnectedUser connectedUser = userAppService.getConnectedUser();
        if (!userAppService.isAdminUser() && !connectedUser.getTenantCode().equals(tenantCode)) {
            String uuid = UUID.randomUUID().toString();
            log.error("[{}]: User [{}] user tenant {} try to read table  of another tenant {}", uuid,
                    connectedUser.getEmployeeNumber(), connectedUser.getTenantCode(), tenantCode);

            throw new ServiceException(uuid, "You don't have permission to read this table");

        }
        return refTablePort.getRefTableById(id, tenantCode)
                .orElseThrow(() -> new ServiceException("NOT_FOUND",
                        "Aucune table trouvée pour id=" + id + " et tenant=" + tenantCode));
    }

    public List<DomainRefTable> getRefTablesByName(String name, String tenantCode) throws ServiceException {
        ConnectedUser connectedUser = userAppService.getConnectedUser();
        if (!userAppService.isAdminUser() && !connectedUser.getTenantCode().equals(tenantCode)) {
            String uuid = UUID.randomUUID().toString();
            log.error("[{}]: User [{}] user tenant {} try to read table  of another tenant {}", uuid,
                    connectedUser.getEmployeeNumber(), connectedUser.getTenantCode(), tenantCode);

            throw new ServiceException(uuid, "You don't have permission to read this table");

        }
        return refTablePort.getRefTablesName(name, tenantCode);
    }
}
