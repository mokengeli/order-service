package com.bacos.mokengeli.biloko.application.service;

import com.bacos.mokengeli.biloko.application.domain.DomainOrder;
import com.bacos.mokengeli.biloko.application.domain.OrderItemState;
import com.bacos.mokengeli.biloko.application.domain.model.ConnectedUser;
import com.bacos.mokengeli.biloko.application.domain.model.CreateOrder;
import com.bacos.mokengeli.biloko.application.domain.model.CreateOrderItem;
import com.bacos.mokengeli.biloko.application.exception.ServiceException;
import com.bacos.mokengeli.biloko.application.port.DishPort;
import com.bacos.mokengeli.biloko.application.port.OrderPort;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OrderService {

    private final OrderPort orderPort;
    private final UserAppService userAppService;
    private final DishPort dishPort;

    @Autowired
    public OrderService(OrderPort orderPort, UserAppService userAppService, DishPort dishPort) {
        this.orderPort = orderPort;
        this.userAppService = userAppService;
        this.dishPort = dishPort;
    }

    public DomainOrder createOrder(Long currencyId, String refTable, List<CreateOrderItem> createOrderItems) throws ServiceException {
        ConnectedUser connectedUser = this.userAppService.getConnectedUser();

        if (StringUtils.isEmpty(refTable)) {
            String errorId = UUID.randomUUID().toString();
            log.error("[{}]: User [{}]. RefTable is mandatory", errorId, connectedUser.getEmployeeNumber());
            throw new ServiceException(errorId, "RefTable cannot be empty");
        }
        String tenantCode = connectedUser.getTenantCode();
        if (!this.orderPort.isRefTableBelongToTenant(refTable, tenantCode)) {
            String errorId = UUID.randomUUID().toString();
            log.error("[{}]: User [{}]. RefTable [{}] provided neither not exist or not belong to the tenant of user", errorId, connectedUser.getEmployeeNumber(), refTable);
            throw new ServiceException(errorId, "Table must belong to your company");
        }

        double totalPrice = getTotalPrice(connectedUser, createOrderItems);
        CreateOrder createOrder = CreateOrder.builder()
                .tenantCode(tenantCode)
                .currencyId(currencyId)
                .employeeNumber(connectedUser.getEmployeeNumber())
                .refTable(refTable)
                .totalPrice(totalPrice)
                .state(OrderItemState.PENDING)
                .orderItems(createOrderItems)
                .build();
        Optional<DomainOrder> order;
        try {
            order = this.orderPort.createOrder(createOrder);
        } catch (ServiceException e) {
            log.error("[{}]: User [{}]. message: {}", e.getTechnicalId(),
                    connectedUser.getEmployeeNumber(), e.getMessage());
            throw new ServiceException(e.getTechnicalId(), "An internal error occurred");
        }
        if (order.isEmpty()) {
            String errorId = UUID.randomUUID().toString();
            log.error("[{}]: User [{}]. Create Order is Empty.", errorId,
                    connectedUser.getEmployeeNumber());
            throw new ServiceException(errorId, "An internal error occurred");
        }
        return order.get();
    }


    public List<DomainOrder> getOrderByState(String state) throws ServiceException {
        OrderItemState orderItemState = OrderItemState.valueOf(state);
        ConnectedUser connectedUser = this.userAppService.getConnectedUser();
        Optional<List<DomainOrder>> optOrder;
        try {
            optOrder = orderPort.getOrdersByState(orderItemState, connectedUser.getTenantCode());

        } catch (ServiceException e) {
            log.error("[{}]: User [{}]. message: {}", e.getTechnicalId(),
                    connectedUser.getEmployeeNumber(), e.getMessage());
            throw new ServiceException(e.getTechnicalId(), "An internal error occurred");
        }
        if (optOrder.isEmpty()) {
            String errorId = UUID.randomUUID().toString();
            log.error("[{}]: User [{}]. Create Order is Empty.", errorId,
                    connectedUser.getEmployeeNumber());
            throw new ServiceException(errorId, "An internal error occurred");
        }
        return optOrder.get();

    }

    private double getTotalPrice(ConnectedUser connectedUser, List<CreateOrderItem> createOrderItems) throws ServiceException {

        double totalPrice = 0.0;
        Set<Long> ids = createOrderItems.stream()
                .map(CreateOrderItem::getDishId)
                .collect(Collectors.toSet());
        if (!dishPort.isAllDishesOfTenant(connectedUser.getTenantCode(), new ArrayList<>(ids))) {
            String errorId = UUID.randomUUID().toString();
            log.error("[{}]: User [{}] try to create order with dish of other tenant code [{}]", errorId,
                    connectedUser.getEmployeeNumber(), connectedUser.getTenantCode());
            throw new ServiceException(errorId, "A problem occured with the dish.");
        }
        for (CreateOrderItem createOrderItem : createOrderItems) {
            Double price = this.dishPort.getDishPrice(createOrderItem.getDishId());
            totalPrice += price * createOrderItem.getCount();
        }
        return totalPrice;
    }
    
    public void changeOrderItemState(Long id, OrderItemState orderItemState) throws ServiceException {
        ConnectedUser connectedUser = this.userAppService.getConnectedUser();
        boolean isOrderItemOfTenant = this.orderPort.isOrderItemOfTenant(id, connectedUser.getTenantCode());
        if (!isOrderItemOfTenant) {
            String errorId = UUID.randomUUID().toString();
            log.error("[{}]: User [{}] try to [{}] orderItem [{}] of other tenant code [{}]", errorId,
                    connectedUser.getEmployeeNumber(), orderItemState, id, connectedUser.getTenantCode());
            throw new ServiceException(errorId, "A problem occured with the dish.");
        }
        try {
            if (OrderItemState.READY.equals(orderItemState)) {
                this.orderPort.prepareOrderItem(id);
            } else {
                this.orderPort.changeOrderItemState(id, orderItemState);
            }

        } catch (ServiceException e) {
            log.error("[{}]: User [{}]. message: {}", e.getTechnicalId(),
                    connectedUser.getEmployeeNumber(), e.getMessage());
            throw new ServiceException(e.getTechnicalId(), "An internal error occurred");
        }
    }
}
