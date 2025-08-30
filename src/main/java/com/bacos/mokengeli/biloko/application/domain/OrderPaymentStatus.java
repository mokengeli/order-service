package com.bacos.mokengeli.biloko.application.domain;

import java.util.List;

public enum OrderPaymentStatus {
    UNPAID,              // Aucun paiement effectué
    PARTIALLY_PAID,      // Une partie de la commande a été payée
    FULLY_PAID,          // Tous les items sont payés au prix complet
    PAID_WITH_DISCOUNT,  // Payée avec une remise
    PAID_WITH_REJECTED_ITEM, // Payée malgré certains items rejetés
    PAID_WITH_RETURNED_ITEM,
    CLOSED_WITH_DEBT;

    public static List<OrderPaymentStatus> getAllPaidStatus() {
        return List.of(OrderPaymentStatus.FULLY_PAID,
                OrderPaymentStatus.PAID_WITH_DISCOUNT,
                OrderPaymentStatus.PAID_WITH_REJECTED_ITEM,
                OrderPaymentStatus.PAID_WITH_RETURNED_ITEM,
                OrderPaymentStatus.CLOSED_WITH_DEBT);
    }
}