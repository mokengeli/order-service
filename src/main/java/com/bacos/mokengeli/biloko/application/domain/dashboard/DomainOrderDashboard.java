package com.bacos.mokengeli.biloko.application.domain.dashboard;

import com.bacos.mokengeli.biloko.application.domain.OrderPaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class DomainOrderDashboard {
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

