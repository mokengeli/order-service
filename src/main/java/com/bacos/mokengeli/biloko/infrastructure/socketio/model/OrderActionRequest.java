package com.bacos.mokengeli.biloko.infrastructure.socketio.model;

import lombok.Data;
import java.util.Map;

@Data
public class OrderActionRequest {
    private Long orderId;
    private String action; // CONFIRM, CANCEL, READY, DELIVERED, PAID
    private Map<String, Object> metadata;
}
