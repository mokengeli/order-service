package com.bacos.mokengeli.biloko.infrastructure.socketio.model;

import lombok.Data;

@Data
public class AuthenticationRequest {
    private String token;
    private String tenantCode;
    private String platform;
    private String appVersion;
}