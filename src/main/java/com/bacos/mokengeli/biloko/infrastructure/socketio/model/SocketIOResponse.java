package com.bacos.mokengeli.biloko.infrastructure.socketio.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SocketIOResponse {
    private boolean success;
    private String message;
    private Object data;
    private String error;
    private String timestamp;
}