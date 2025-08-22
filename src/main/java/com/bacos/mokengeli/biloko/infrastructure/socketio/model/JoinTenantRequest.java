package com.bacos.mokengeli.biloko.infrastructure.socketio.model;

import lombok.Data;

@Data
public class JoinTenantRequest {
    private String tenantCode;
}