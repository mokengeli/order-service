package com.bacos.mokengeli.biloko.application.domain.model;


import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class OrderNotification {

    private String tenantCode;
    private Long orderId;
    private String newState;
    private String previousState;
    private OrderNotificationStatus orderStatus;
    private LocalDateTime timestamp;


    public enum OrderNotificationStatus {
        NEW_ORDER,
        DISH_UPDATE,
        PAYMENT_UPDATE
    }
}
