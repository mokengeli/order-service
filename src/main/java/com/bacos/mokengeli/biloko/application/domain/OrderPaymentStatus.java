package com.bacos.mokengeli.biloko.application.domain;

public enum OrderPaymentStatus {
    UNPAID,              // Aucun paiement effectué
    PARTIALLY_PAID,      // Une partie de la commande a été payée
    FULLY_PAID,          // Tous les items sont payés au prix complet
    PAID_WITH_DISCOUNT,  // Payée avec une remise
    PAID_WITH_REJECTED_ITEM // Payée malgré certains items rejetés
}