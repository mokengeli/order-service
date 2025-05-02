package com.bacos.mokengeli.biloko.presentation;

import lombok.Data;

import java.util.List;

@Data
public class CreateOrderRequest {
    private Long tableId;
    private String tableName;
    private Long currencyId;
    private List<CreateOrderItemRequest> orderItems;


    @Data
    public static class CreateOrderItemRequest {
        private Long dishId;
        private String note;
        private int count;
    }

}
