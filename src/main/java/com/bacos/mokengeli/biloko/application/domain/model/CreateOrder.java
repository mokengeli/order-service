package com.bacos.mokengeli.biloko.application.domain.model;

import com.bacos.mokengeli.biloko.application.domain.OrderState;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class CreateOrder {

    private String tenantCode;
    private String refTable;
    private String employeeNumber;
    private Long currencyId;
    private List<CreateOrderItem> orderItems;
    private OrderState state;
    private double totalPrice;

}
