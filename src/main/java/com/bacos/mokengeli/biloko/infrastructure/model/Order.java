package com.bacos.mokengeli.biloko.infrastructure.model;

import com.bacos.mokengeli.biloko.application.domain.OrderItemState;
import com.bacos.mokengeli.biloko.application.domain.OrderPaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "total_price", nullable = false)
    private Double totalPrice;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items;

    @ManyToOne
    @JoinColumn(name = "ref_table_id", nullable = false)
    private RefTable refTable;

    @ManyToOne
    @JoinColumn(name = "currency_id", nullable = false)
    private Currency currency;
    @ManyToOne
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status")
    private OrderPaymentStatus paymentStatus = OrderPaymentStatus.UNPAID;

    @Column(name = "paid_amount", nullable = false)
    private Double paidAmount = 0.0;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PaymentTransaction> payments;

    public void addItem(OrderItem item) {
        if (items == null) {
            items = new ArrayList<>();
        }
        items.add(item);
        item.setOrder(this);  // Set parent reference
    }

    // Nouvelle méthode pour ajouter un paiement
    public void addPayment(PaymentTransaction payment) {
        if (payments == null) {
            payments = new ArrayList<>();
        }
        payments.add(payment);
        payment.setOrder(this);

        // Mettre à jour le montant payé
        this.paidAmount += payment.getAmount();

        // Mettre à jour le statut de paiement
        updatePaymentStatus();
    }

    // Méthode pour calculer le montant restant à payer
    public Double getRemainingAmount() {
        return Math.max(0, totalPrice - paidAmount);
    }

    // Mise à jour du statut de paiement
    private void updatePaymentStatus() {
        double remainingAmount = getRemainingAmount();
        boolean hasDiscount = payments.stream().anyMatch(p -> p.getDiscountAmount() > 0);
        boolean hasRejectedItems = items.stream().anyMatch(i -> i.getState() == OrderItemState.REJECTED);

        if (remainingAmount <= 0.01) { // Tolérance pour erreurs d'arrondi
            if (hasRejectedItems) {
                paymentStatus = OrderPaymentStatus.PAID_WITH_REJECTED_ITEM;
            } else if (hasDiscount) {
                paymentStatus = OrderPaymentStatus.PAID_WITH_DISCOUNT;
            } else {
                paymentStatus = OrderPaymentStatus.FULLY_PAID;
            }

            // Marquer tous les éléments non rejetés comme payés
            items.forEach(item -> {
                if (item.getState() != OrderItemState.REJECTED) {
                    item.setState(OrderItemState.PAID);
                }
            });
        } else if (paidAmount > 0) {
            paymentStatus = OrderPaymentStatus.PARTIALLY_PAID;
        } else {
            paymentStatus = OrderPaymentStatus.UNPAID;
        }
    }

}