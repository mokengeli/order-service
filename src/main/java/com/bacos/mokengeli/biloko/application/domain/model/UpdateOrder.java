package com.bacos.mokengeli.biloko.application.domain.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class UpdateOrder {
    private Long orderId;
    private List<CreateOrderItem> orderItems;
}
