package com.bacos.mokengeli.biloko.application.domain;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Builder
@Data
public class DomainOrder {
    private Long id;
    private String tenantCode;
    private String tableName;
    private Long tableId;
    private String employeeNumber;  // Waiter identifier by employee number
    private List<DomainOrderItem> items;
    private double totalPrice;
    private DomainCurrency currency;
    private OffsetDateTime orderDate;
    private OrderPaymentStatus paymentStatus;
    private double paidAmount;
    private double remainingAmount;
    private List<DomainPaymentTransaction> payments;

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
        private OffsetDateTime orderItemDate;
        private List<String> categories;

    }
    @Builder
    @Data
    public static class DomainPaymentTransaction {
        private Long id;
        private double amount;
        private String paymentMethod;
        private OffsetDateTime createdAt;
        private String employeeNumber;
        private String notes;
        private boolean isRefund;
        private double discountAmount;
    }
    public void sortItemsByCategoryPriority() {
        if (items == null || items.isEmpty()) {
            return;
        }

        // Définir les priorités de catégories
        Map<String, Integer> categoryPriority = Map.of(
                "boisson", 1,
                "beverage", 1,
                "starter", 2,
                "entrée", 2,
                "dessert", 3
        );

        items.sort(Comparator
                .comparingInt((DomainOrderItem item) -> {
                    // Chercher la plus haute priorité parmi les catégories de l'item
                    if (item.getCategories() == null || item.getCategories().isEmpty()) {
                        return Integer.MAX_VALUE; // Items sans catégorie -> tout à la fin
                    }
                    return item.getCategories().stream()
                            .map(String::toLowerCase)
                            .map(cat -> categoryPriority.getOrDefault(cat, Integer.MAX_VALUE))
                            .min(Integer::compareTo)
                            .orElse(Integer.MAX_VALUE);
                })
                .thenComparing(item -> item.getDishName() != null ? item.getDishName().toLowerCase() : "")
        );
    }

}

