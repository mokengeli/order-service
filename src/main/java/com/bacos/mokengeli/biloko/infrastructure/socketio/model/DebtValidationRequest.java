package com.bacos.mokengeli.biloko.infrastructure.socketio.model;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class DebtValidationRequest {
    private Long customerId;
    private Long orderId;
    private BigDecimal amount;
    private String validationType; // APPROVE, REJECT, PARTIAL
    private String notes;
}