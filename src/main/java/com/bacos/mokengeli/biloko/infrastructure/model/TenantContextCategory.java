package com.bacos.mokengeli.biloko.infrastructure.model;


import jakarta.persistence.Entity;
import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@Data
@NoArgsConstructor
@Entity
@Table(name = "tenant_context_categories")
@IdClass(TenantContextCategoryId.class)
public class TenantContextCategory {

    @Id
    @ManyToOne
    @JoinColumn(name = "tenant_context_id", nullable = false)
    private TenantContext tenantContext;

    @Id
    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
}
