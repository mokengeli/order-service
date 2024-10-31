package com.bacos.mokengeli.biloko.application.service;


import com.bacos.mokengeli.biloko.application.domain.DomainRefTable;
import com.bacos.mokengeli.biloko.application.domain.model.ConnectedUser;
import com.bacos.mokengeli.biloko.application.port.OrderPort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class RefTableService {

    private final UserAppService userAppService;
    private final OrderPort orderPort;

    public RefTableService(UserAppService userAppService, OrderPort orderPort) {
        this.userAppService = userAppService;
        this.orderPort = orderPort;
    }

    public List<DomainRefTable> getRefTables() {
        ConnectedUser connectedUser = this.userAppService.getConnectedUser();
        String tenantCode = connectedUser.getTenantCode();

        Optional<List<DomainRefTable>> optRef = orderPort.getRefTablesByTenantCode(tenantCode);
        return optRef.orElseGet(ArrayList::new);
    }
}
