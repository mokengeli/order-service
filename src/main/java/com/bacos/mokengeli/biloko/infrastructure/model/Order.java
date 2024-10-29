package com.bacos.mokengeli.biloko.infrastructure.model;

import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;

import java.time.LocalDateTime;
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


    @Column(name = "ref_table")
    private String refTable;

    @Column(name = "state", nullable = false)
    private String state;

    @Column(name = "total_price", nullable = false)
    private Double totalPrice;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items;


    @ManyToOne
    @JoinColumn(name = "currency_id", nullable = false)
    private Currency currency;
    @ManyToOne
    @JoinColumn(name = "tenant_context_id", nullable = false)
    private TenantContext tenantContext;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public void addItem(OrderItem item) {
        if (items == null) {
            items = new ArrayList<>();
        }
        items.add(item);
        item.setOrder(this);  // Set parent reference
    }

}