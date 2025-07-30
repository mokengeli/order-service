package com.bacos.mokengeli.biloko.application.service;


import com.bacos.mokengeli.biloko.application.domain.model.ConnectedUser;
import com.bacos.mokengeli.biloko.application.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
public class TenantService {
    private final TenantPort tenantPort;
    private final UserAppService userAppService;
    @Autowired
    public TenantService(TenantPort tenantPort, UserAppService userAppService) {
        this.tenantPort = tenantPort;
        this.userAppService = userAppService;
    }

    public String getTenantName(String tenantCode) throws ServiceException {
            ConnectedUser connectedUser = userAppService.getConnectedUser();
            if (!userAppService.isAdminUser() &&
                    !connectedUser.getTenantCode().equals(tenantCode)) {
                String uuid = UUID.randomUUID().toString();
                log.error("[{}]: User [{}] Tenant [{}] try to get the fetchDailyHourlyMatrix of another tenant: {}", uuid,
                        connectedUser.getEmployeeNumber(), connectedUser.getTenantCode(), tenantCode);
                throw new ServiceException(uuid, "Accès refusé pour ce tenant");
            }
            return tenantPort.getTenantName(tenantCode);
    }
}
