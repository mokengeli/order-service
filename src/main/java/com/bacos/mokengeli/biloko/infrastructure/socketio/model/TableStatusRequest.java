package com.bacos.mokengeli.biloko.infrastructure.socketio.model;

import lombok.Data;

@Data
public class TableStatusRequest {
    private Long tableId;
    private String newStatus; // FREE, OCCUPIED, RESERVED
    private String reason;
}