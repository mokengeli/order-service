package com.bacos.mokengeli.biloko.infrastructure.socketio.model;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
public class SocketIONotification {
    private Long orderId;
    private Long tableId;
    private Long itemId;
    private String tenantCode;
    private String orderStatus;
    private String newState;
    private String previousState;
    private String tableState;
    private LocalDateTime timestamp;
    private Map<String, Object> metadata;
}