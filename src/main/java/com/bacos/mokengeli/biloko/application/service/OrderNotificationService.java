package com.bacos.mokengeli.biloko.application.service;

import com.bacos.mokengeli.biloko.application.domain.model.ConnectedUser;
import com.bacos.mokengeli.biloko.application.domain.model.OrderNotification;
import com.bacos.mokengeli.biloko.application.port.OrderNotificationPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
public class OrderNotificationService {
    private final UserAppService userAppService;

    private final OrderNotificationPort orderNotification;

    @Autowired
    public OrderNotificationService(UserAppService userAppService, OrderNotificationPort orderNotification) {
        this.userAppService = userAppService;
        this.orderNotification = orderNotification;
    }

    public void notifyStateChange(Long orderId, Long tableId, OrderNotification.OrderNotificationStatus orderNotificationStatus,
                                  String previousState, String newState, String tableState, String additionnalInfo) {
        ConnectedUser connectedUser = this.userAppService.getConnectedUser();

        String tenantCode = connectedUser.getTenantCode();
        OrderNotification notification = OrderNotification.builder()
                .orderId(orderId)
                .tableId(tableId)
                .previousState(previousState)
                .newState(newState)
                .tenantCode(tenantCode)
                .orderStatus(orderNotificationStatus)
                .tableState(tableState)
                .timestamp(LocalDateTime.now())
                .message(additionnalInfo)
                .build();
        this.orderNotification.notifyWebSocketUser(notification);
    }

    public void notifyDebtValidation(Long orderId, Long tableId, OrderNotification.OrderNotificationStatus orderNotificationStatus,
                                   String previousState, String newState, String tableState, 
                                   String message, Long validationRequestId) {
        ConnectedUser connectedUser = this.userAppService.getConnectedUser();

        String tenantCode = connectedUser.getTenantCode();
        OrderNotification notification = OrderNotification.builder()
                .orderId(orderId)
                .tableId(tableId)
                .previousState(previousState)
                .newState(newState)
                .tenantCode(tenantCode)
                .orderStatus(orderNotificationStatus)
                .tableState(tableState)
                .timestamp(LocalDateTime.now())
                .message(message)
                .validationRequestId(validationRequestId)
                .build();
        this.orderNotification.notifyWebSocketUser(notification);
    }

    public void notifyStateChange(Long orderId, Long tableId, OrderNotification.OrderNotificationStatus orderNotificationStatus,
                                  String previousState, String newState, String tableState) {
        notifyStateChange(orderId, tableId, orderNotificationStatus, previousState, newState, tableState, null);
    }
}
