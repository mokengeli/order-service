package com.bacos.mokengeli.biloko.infrastructure.model;

import com.bacos.mokengeli.biloko.application.domain.OrderItemState;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne
    @JoinColumn(name = "dish_id", nullable = false)
    private Dish dish;

    @Column(name = "state", nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderItemState state;

    @ManyToOne
    @JoinColumn(name = "currency_id", nullable = false)
    private Currency currency;

    @Column(name = "note")
    private String note;

    @Column(name = "unit_price", nullable = false)
    private Double unitPrice;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

}
