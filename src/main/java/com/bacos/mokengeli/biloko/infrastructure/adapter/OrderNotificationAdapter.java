package com.bacos.mokengeli.biloko.infrastructure.adapter;

import com.bacos.mokengeli.biloko.application.domain.model.OrderNotification;
import com.bacos.mokengeli.biloko.application.port.OrderNotificationPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderNotificationAdapter implements OrderNotificationPort {
    private final SimpMessagingTemplate messagingTemplate;
    private static final String ORDER_DESTINATION = "/topic/orders/";

    @Autowired
    public OrderNotificationAdapter(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void notifyWebSocketUser(OrderNotification notification) {
        String destination = ORDER_DESTINATION + notification.getTenantCode();
        messagingTemplate.convertAndSend(destination, notification);
    }
}
