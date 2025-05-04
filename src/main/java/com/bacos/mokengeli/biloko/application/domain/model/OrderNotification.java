package com.bacos.mokengeli.biloko.application.domain.model;


import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class OrderNotification {

    private Long orderId;
    private Long tableId;
    private String tenantCode;
    private String newState;
    private String previousState;
    private String tableState; // FREE, OCCUPIED, RESERVED
    private OrderNotificationStatus orderStatus;
    private LocalDateTime timestamp;



    public enum OrderNotificationStatus {
        NEW_ORDER,
        DISH_UPDATE,
        PAYMENT_UPDATE,
        TABLE_STATUS_UPDATE
    }
}
