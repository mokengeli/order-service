package com.bacos.mokengeli.biloko.application.service;

import com.bacos.mokengeli.biloko.application.domain.model.ConnectedUser;
import com.bacos.mokengeli.biloko.application.domain.model.OrderNotification;
import com.bacos.mokengeli.biloko.application.port.OrderNotificationPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public void notifyStateChange(Long orderId, String previousState, String newState) {
        ConnectedUser connectedUser = this.userAppService.getConnectedUser();

        String tenantCode = connectedUser.getTenantCode();
        OrderNotification notification = OrderNotification.builder()
                .orderId(orderId)
                .previousState(previousState)
                .newState(newState)
                .tenantCode(tenantCode)
                .build();
        this.orderNotification.notifyWebSocketUser(notification);
    }
}
