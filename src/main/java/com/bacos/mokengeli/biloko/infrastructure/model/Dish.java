package com.bacos.mokengeli.biloko.infrastructure.model;

import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@AllArgsConstructor
@Data
@NoArgsConstructor
@Entity
@Table(name = "dishes")
public class Dish {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "current_price", nullable = false)
    private Double currentPrice; // Holds the current price

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "tenant_context_id", nullable = false)
    private TenantContext tenantContext;  // Link to TenantContext

    @OneToMany(mappedBy = "dish", cascade = CascadeType.ALL)
    private List<DishArticle> dishArticles;

    @OneToMany(mappedBy = "dish", cascade = CascadeType.ALL)
    private List<DishPriceHistory> priceHistory; // Link to price history
}