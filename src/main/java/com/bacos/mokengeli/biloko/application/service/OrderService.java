package com.bacos.mokengeli.biloko.application.service;

import com.bacos.mokengeli.biloko.application.domain.DomainOrder;
import com.bacos.mokengeli.biloko.application.domain.OrderItemState;
import com.bacos.mokengeli.biloko.application.domain.OrderPaymentStatus;
import com.bacos.mokengeli.biloko.application.domain.TableState;
import com.bacos.mokengeli.biloko.application.domain.model.*;
import com.bacos.mokengeli.biloko.application.exception.ServiceException;
import com.bacos.mokengeli.biloko.application.port.DishPort;
import com.bacos.mokengeli.biloko.application.port.OrderPort;
import lombok.extern.slf4j.Slf4j;
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
    private final OrderNotificationService orderNotificationService;

    @Autowired
    public OrderService(OrderPort orderPort, UserAppService userAppService, DishPort dishPort, OrderNotificationService orderNotificationService) {
        this.orderPort = orderPort;
        this.userAppService = userAppService;
        this.dishPort = dishPort;
        this.orderNotificationService = orderNotificationService;
    }

    public DomainOrder createOrder(Long currencyId, Long refTableId, List<CreateOrderItem> createOrderItems) throws ServiceException {
        ConnectedUser connectedUser = this.userAppService.getConnectedUser();

        if (refTableId == null) {
            String errorId = UUID.randomUUID().toString();
            log.error("[{}]: User [{}]. RefTableId is mandatory", errorId, connectedUser.getEmployeeNumber());
            throw new ServiceException(errorId, "RefTable cannot be empty");
        }
        String tenantCode = connectedUser.getTenantCode();
        if (!this.orderPort.isRefTableBelongToTenant(refTableId, tenantCode)) {
            String errorId = UUID.randomUUID().toString();
            log.error("[{}]: User [{}]. RefTable [{}] provided neither not exist or not belong to the tenant of user", errorId, connectedUser.getEmployeeNumber(), refTableId);
            throw new ServiceException(errorId, "Table must belong to your company");
        }

        double totalPrice = getTotalPrice(connectedUser, createOrderItems);
        CreateOrder createOrder = CreateOrder.builder()
                .tenantCode(tenantCode)
                .currencyId(currencyId)
                .employeeNumber(connectedUser.getEmployeeNumber())
                .tableId(refTableId)
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
        this.orderNotificationService.notifyStateChange(order.get().getId(), refTableId,
                OrderNotification.OrderNotificationStatus.NEW_ORDER,
                "", OrderItemState.PENDING.name(), TableState.OCCUPIED.name());

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

    public void changeOrderItemState(Long orderItemId, OrderItemState orderItemState) throws ServiceException {
        ConnectedUser connectedUser = this.userAppService.getConnectedUser();
        boolean isOrderItemOfTenant = this.orderPort.isOrderItemOfTenant(orderItemId, connectedUser.getTenantCode());
        if (!isOrderItemOfTenant) {
            String errorId = UUID.randomUUID().toString();
            log.error("[{}]: User [{}] try to [{}] orderItem [{}] of other tenant code [{}]", errorId,
                    connectedUser.getEmployeeNumber(), orderItemState, orderItemId, connectedUser.getTenantCode());
            throw new ServiceException(errorId, "A problem occured with the dish.");
        }
        Optional<DomainOrder> OptOrder = this.orderPort.getOrderByOrderItemId(orderItemId);
        if (OptOrder.isEmpty()) {
            String errorId = UUID.randomUUID().toString();
            log.error("[{}]: User [{}]. No Order found for this orderItemId {} that exist on database.", errorId,
                    connectedUser.getEmployeeNumber(), orderItemId);
            throw new ServiceException(errorId, "A server error occurred.");
        }
        DomainOrder domainOrder = OptOrder.get();
        try {
            OrderItemState currentState = this.orderPort.getOrderItemState(orderItemId);

            if (OrderItemState.READY.equals(orderItemState)) {
                this.orderPort.prepareOrderItem(orderItemId);
            } else {
                this.orderPort.changeOrderItemState(orderItemId, orderItemState);
            }

            boolean isTableFree = this.orderPort.isTableFree(domainOrder.getTableId());
            if (isTableFree) {
                this.orderNotificationService.notifyStateChange(domainOrder.getId(), domainOrder.getTableId(),
                        OrderNotification.OrderNotificationStatus.DISH_UPDATE, currentState.name(), orderItemState.name(),
                        TableState.FREE.name());
                return;
            }

            this.orderNotificationService.notifyStateChange(domainOrder.getId(), domainOrder.getTableId(),
                    OrderNotification.OrderNotificationStatus.DISH_UPDATE, currentState.name(), orderItemState.name(),
                    TableState.OCCUPIED.name());
        } catch (ServiceException e) {
            log.error("[{}]: User [{}]. message: {}", e.getTechnicalId(),
                    connectedUser.getEmployeeNumber(), e.getMessage());
            throw new ServiceException(e.getTechnicalId(), "An internal error occurred");
        }
    }

    public List<DomainOrder> getActiveOrdersByTable(Long tableId) throws ServiceException {
        ConnectedUser connectedUser = this.userAppService.getConnectedUser();
        if (!this.orderPort
                .isRefTableBelongToTenant(tableId, connectedUser.getTenantCode())) {
            String errorId = UUID.randomUUID().toString();
            log.error("[{}]: User [{}] of tenant {} don't have the right to get orders for refTableId {}", errorId, connectedUser.getEmployeeNumber(),
                    connectedUser.getTenantCode(), tableId);
            throw new ServiceException(errorId, "You can't add item owning by another partener");
        }

        return this.orderPort.getActiveOrdersByTable(tableId);
    }

    public DomainOrder addItems(UpdateOrder order) throws ServiceException {
        ConnectedUser connectedUser = this.userAppService.getConnectedUser();
        String tenantCode = connectedUser.getTenantCode();
        List<Long> ids = order.getOrderItems()
                .stream()
                .map(CreateOrderItem::getDishId)
                .toList();
        if (!this.orderPort.isOrderBelongToTenant(order.getOrderId(), tenantCode)) {
            String errorId = UUID.randomUUID().toString();
            log.error("[{}]: User [{}] try to update order with dish of other tenant code [{}]", errorId,
                    connectedUser.getEmployeeNumber(), connectedUser.getTenantCode());
            throw new ServiceException(errorId, "You don't have the right to update dish of other tenant code. .");
        }

        if (!dishPort.isAllDishesOfTenant(tenantCode,
                new ArrayList<>(ids))) {
            String errorId = UUID.randomUUID().toString();
            log.error("[{}]: User [{}] try to update order with dish of other tenant code [{}]", errorId,
                    connectedUser.getEmployeeNumber(), connectedUser.getTenantCode());
            throw new ServiceException(errorId, "You don't have the right to update dish of other tenant code. .");
        }

        try {
            DomainOrder domainOrder = this.orderPort.addItems(order);
            this.orderNotificationService.notifyStateChange(domainOrder.getId(),
                    domainOrder.getTableId(),
                    OrderNotification.OrderNotificationStatus.DISH_UPDATE, OrderItemState.PENDING.name(),
                    OrderItemState.PENDING.name(), TableState.OCCUPIED.name());
            return domainOrder;
        } catch (ServiceException e) {
            log.error("[{}]: User [{}]. message: {}", e.getTechnicalId(),
                    connectedUser.getEmployeeNumber(), e.getMessage());
            throw new ServiceException(e.getTechnicalId(), "An internal error occurred while updating order");
        }
    }

    public DomainOrder getOrderById(Long id) throws ServiceException {
        ConnectedUser connectedUser = this.userAppService.getConnectedUser();
        boolean orderBelongToTenant = this.orderPort.isOrderBelongToTenant(id, connectedUser.getTenantCode());
        if (!orderBelongToTenant) {
            String errorId = UUID.randomUUID().toString();
            log.error("[{}]: User [{}] try to get order of another tenant code order Id = [{}]", errorId,
                    connectedUser.getEmployeeNumber(), id);
            throw new ServiceException(errorId, "You don't have the right to get this order.");
        }
        Optional<DomainOrder> order = this.orderPort.getOrder(id);
        if (order.isEmpty()) {
            String errorId = UUID.randomUUID().toString();
            log.error("[{}]: User [{}] no order found for this id = {}", errorId,
                    connectedUser.getEmployeeNumber(), id);
            throw new ServiceException(errorId, "No order found for this order.");

        }
        return order.get();
    }
}
