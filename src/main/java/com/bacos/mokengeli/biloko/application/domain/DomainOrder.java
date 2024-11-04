package com.bacos.mokengeli.biloko.application.domain;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class DomainOrder {
    private Long id;
    private String tenantCode;
    private String refTable;
    private String employeeNumber;  // Waiter identifier by employee number
    private List<DomainOrderItem> items;
    private double totalPrice;
    private DomainCurrency currency;

    @Builder
    @Data
    public static class DomainOrderItem {
        private Long id;
        private Long dishId;
        private String dishName;
        private String note;
        private int count;
        private OrderItemState state;
        private Double unitPrice;
    }

}

