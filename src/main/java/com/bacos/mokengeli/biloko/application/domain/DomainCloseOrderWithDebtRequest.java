package com.bacos.mokengeli.biloko.application.domain;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class DomainCloseOrderWithDebtRequest {
    private Long orderId;
    private String reason;
    private String validationType; // "IMMEDIATE" or "REMOTE"
    private Integer validationCode; // optional
    private double amount;

}