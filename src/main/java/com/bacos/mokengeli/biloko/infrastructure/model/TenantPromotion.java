package com.bacos.mokengeli.biloko.infrastructure.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "tenant_promotions")
public class TenantPromotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "tenant_context_id", nullable = false)
    private TenantContext tenantContext;  // Link to TenantContext

    @ManyToOne
    @JoinColumn(name = "dish_id")
    private Dish dish;  // Link to Dish

    @ManyToOne
    @JoinColumn(name = "menu_id")
    private Menu menu;  // Link to Menu

    @Column(name = "discount_percentage", nullable = false)
    private Double discountPercentage;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
}
