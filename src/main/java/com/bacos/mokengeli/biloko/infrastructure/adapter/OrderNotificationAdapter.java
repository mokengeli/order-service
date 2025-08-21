package com.bacos.mokengeli.biloko.infrastructure.adapter;

import com.bacos.mokengeli.biloko.application.domain.model.OrderNotification;
import com.bacos.mokengeli.biloko.application.port.OrderNotificationPort;
import com.corundumstudio.socketio.SocketIOServer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderNotificationAdapter implements OrderNotificationPort {

    private final SocketIOServer server;
    private static final String ROOM_PREFIX = "tenant:";

    @Override
    public void sendOrderNotification(OrderNotification notification) {
        server.getRoomOperations(ROOM_PREFIX + notification.getTenantCode())
                .sendEvent("order:notification", notification);
    }

    @Override
    public void sendTableUpdate(String tenantCode, Object payload) {
        server.getRoomOperations(ROOM_PREFIX + tenantCode)
                .sendEvent("table:update", payload);
    }

    @Override
    public void sendDishReady(String tenantCode, Object payload) {
        server.getRoomOperations(ROOM_PREFIX + tenantCode)
                .sendEvent("dish:ready", payload);
    }
}
