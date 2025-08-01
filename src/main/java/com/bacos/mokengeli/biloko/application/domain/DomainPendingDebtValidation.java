package com.bacos.mokengeli.biloko.application.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DomainPendingDebtValidation {
    private Long id;
    private Long orderId;
    private Long tableId;
    private String tableName;
    private double amount;
    private String currency;
    private String reason;
    private String serverName;
    private OffsetDateTime createdAt;
    private String status;
}