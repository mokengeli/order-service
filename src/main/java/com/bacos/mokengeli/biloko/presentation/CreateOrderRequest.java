package com.bacos.mokengeli.biloko.presentation;

import lombok.Data;

import java.util.List;

@Data
public class CreateOrderRequest {
    private String refTable;
    private Long currencyId;
    private List<CreateOrderItemRequest> orderItems;


    @Data
    public static class CreateOrderItemRequest {
        private Long dishId;
        private String note;
        private int count;
    }

}
