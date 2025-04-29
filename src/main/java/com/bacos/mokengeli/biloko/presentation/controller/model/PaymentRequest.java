package com.bacos.mokengeli.biloko.presentation.controller.model;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public  class PaymentRequest {
    private Long orderId;
    private Double amount;
    private String paymentMethod;
    private String notes;
    private Double discountAmount;
}