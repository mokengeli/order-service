package com.bacos.mokengeli.biloko.application.domain.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateOrderItem {
    private Long dishId;
    private String note;
    private int count;
    private Long unitPrice;
}
