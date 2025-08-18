package com.bacos.mokengeli.biloko.infrastructure.adapter;

import com.bacos.mokengeli.biloko.application.domain.model.OrderNotification;
import com.bacos.mokengeli.biloko.application.port.OrderNotificationPort;
import com.corundumstudio.socketio.SocketIOServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OrderNotificationAdapter implements OrderNotificationPort {

    private final SocketIOServer socketIOServer;

    @Autowired
    public OrderNotificationAdapter(SocketIOServer socketIOServer) {
        this.socketIOServer = socketIOServer;
    }

    @Override
    public void notifyWebSocketUser(OrderNotification notification) {
        String room = notification.getTenantCode();
        socketIOServer.getRoomOperations(room).sendEvent("order-notification", notification);
    }
}
