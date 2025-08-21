package com.bacos.mokengeli.biloko.application.port;

import com.bacos.mokengeli.biloko.application.domain.model.OrderNotification;

public interface OrderNotificationPort {

    void sendOrderNotification(OrderNotification notification);

    void sendTableUpdate(String tenantCode, Object payload);

    void sendDishReady(String tenantCode, Object payload);
}
