package com.bacos.mokengeli.biloko.application.domain.model;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderNotification {

    private String tenantCode;
    private Long orderId;
    private String newState;
    private String previousState;


}
