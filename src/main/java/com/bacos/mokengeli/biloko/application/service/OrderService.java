package com.bacos.mokengeli.biloko.application.service;

import com.bacos.mokengeli.biloko.application.domain.DomainOrder;
import com.bacos.mokengeli.biloko.application.domain.OrderState;
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
        double totalPrice = getTotalPrice(connectedUser, createOrderItems);
        CreateOrder createOrder = CreateOrder.builder()
                .tenantCode(tenantCode)
                .currencyId(currencyId)
                .employeeNumber(connectedUser.getEmployeeNumber())
                .refTable(refTable)
                .totalPrice(totalPrice)
                .state(OrderState.PENDING)
                .orderItems(createOrderItems)
                .build();
        Optional<DomainOrder> order = this.orderPort.createOrder(createOrder);
        if (order.isEmpty()) {
            String errorId = UUID.randomUUID().toString();
            log.error("[{}]: User [{}]. Create Order is Empty.", errorId,
                    connectedUser.getEmployeeNumber());
            throw new ServiceException(errorId, "An internal error occurred");
        }
        return order.get();
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


}
