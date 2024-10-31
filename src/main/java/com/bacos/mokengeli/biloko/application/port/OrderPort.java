package com.bacos.mokengeli.biloko.application.port;

import com.bacos.mokengeli.biloko.application.domain.DomainOrder;
import com.bacos.mokengeli.biloko.application.domain.DomainRefTable;
import com.bacos.mokengeli.biloko.application.domain.OrderState;
import com.bacos.mokengeli.biloko.application.domain.model.CreateOrder;
import com.bacos.mokengeli.biloko.application.exception.ServiceException;

import java.util.List;
import java.util.Optional;

public interface OrderPort {
    Optional<DomainOrder> createOrder(CreateOrder order) throws ServiceException;

    Optional<List<DomainOrder>>  getOrdersByState(OrderState orderState, String tenantCode) throws ServiceException;
    boolean isRefTableBelongToTenant(String refTableName, String tenantCode);
    Optional<List<DomainRefTable>> getRefTablesByTenantCode(String tenantCode);
}
