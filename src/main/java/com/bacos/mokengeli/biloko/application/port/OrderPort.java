package com.bacos.mokengeli.biloko.application.port;

import com.bacos.mokengeli.biloko.application.domain.DomainOrder;
import com.bacos.mokengeli.biloko.application.domain.DomainRefTable;
import com.bacos.mokengeli.biloko.application.domain.OrderItemState;
import com.bacos.mokengeli.biloko.application.domain.model.CreateOrder;
import com.bacos.mokengeli.biloko.application.exception.ServiceException;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

public interface OrderPort {
    Optional<DomainOrder> createOrder(CreateOrder order) throws ServiceException;

    Optional<List<DomainOrder>> getOrdersByState(OrderItemState orderState, String tenantCode) throws ServiceException;

    boolean isRefTableBelongToTenant(String refTableName, String tenantCode);

    Page<DomainRefTable> getRefTablesByTenantCode(String tenantCode, int page, int size);

    boolean isOrderItemOfTenant(Long id, String tenantCode);

    void prepareOrderItem(Long id) throws ServiceException;

    void changeOrderItemState(Long id, OrderItemState orderItemState) throws ServiceException;

    OrderItemState getOrderItemState(Long id) throws ServiceException;

    DomainRefTable createRefTable(DomainRefTable refTable) throws ServiceException;

    List<DomainOrder> getActiveOrdersByTable(Long refTableId);

    boolean isRefTableBelongToTenant(Long refTableId, String tenantCode);
}
