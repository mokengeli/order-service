package com.bacos.mokengeli.biloko.application.port;

import com.bacos.mokengeli.biloko.application.domain.model.OrderNotification;

public interface OrderNotificationPort {

    void notifyWebSocketUser(OrderNotification notification);
}
