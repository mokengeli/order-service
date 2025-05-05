package com.bacos.mokengeli.biloko.application.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DomainDish {
    private Long id;
    private String name;
    private Double price;
    private DomainCurrency currency;
    private String tenantCode;
    private List<String> categories;
    private List<DomainDishProduct> dishProducts;

    @Data
    @AllArgsConstructor
    public static class DomainBreakdown {
        /**
         * total CA des paiement sans discount, rejets de certains items etc
         */
        private double fullPayments;
        /**
         * total CA dans les cas de discount, rejets de certains items, etc
         */
        private double discountedPayments;
    }

    @Data
    @AllArgsConstructor
    public static class DomainOrderDashboard {
        private Long orderId;
        private LocalDate date;
        /**
         * Prix de la commande
         */
        private double totalAmount;
        /**
         * Montant payé
         */
        private double paidAmount;
        private OrderPaymentStatus paymentStatus;
    }

    @Data
    @AllArgsConstructor
    public static class DomainRevenueDashboard {
        /**
         * CA real correspondant au montant réellement percus pour les commandes
         */
        private double realRevenue;
        /**
         * CA theorique c'est a dire correspondant a la somme des prix commandes
         */
        private double theoreticalRevenue;
        private DomainCurrency currency;
        private List<DomainOrderDashboard> orders;
        private DomainBreakdown breakdown;
    }
}
