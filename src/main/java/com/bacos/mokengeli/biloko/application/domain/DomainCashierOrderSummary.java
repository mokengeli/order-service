package com.bacos.mokengeli.biloko.application.domain;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;

@Builder
@Data
public class DomainCashierOrderSummary {
    private String date;
    private int totalOrders;
    private double totalRevenue;
    private List<DomainCashierOrder> orders;

    @Builder
    @Data
    public static class DomainCashierOrder {
        private Long orderId;
        private Long tableId;
        private String tableName;
        private double totalAmount;
        private double paidAmount;
        private double remainingAmount;
        private String status;
        private OffsetDateTime createdAt;
        private DomainDishesStatus dishesStatus;
        private int waitingTime; // in minutes

        @Builder
        @Data
        public static class DomainDishesStatus {
            private int total;
            private int ready;
            private int inProgress;
            private int served;
        }
    }
}