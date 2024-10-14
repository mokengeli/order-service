package com.bacos.mokengeli.biloko.infrastructure.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@AllArgsConstructor
@Data
@NoArgsConstructor
@Entity
@Table(name = "tenant_context")
public class TenantContext {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_code", nullable = false, unique = true)
    private String tenantCode;

    @Column(name = "tenant_name", nullable = false)
    private String tenantName;

    @OneToMany(mappedBy = "tenantContext", cascade = CascadeType.ALL)
    private List<Dish> dishes;

    @OneToMany(mappedBy = "tenantContext", cascade = CascadeType.ALL)
    private List<Menu> menus;

    @OneToMany(mappedBy = "tenantContext", cascade = CascadeType.ALL)
    private List<Article> articles;

    @OneToMany(mappedBy = "tenantContext", cascade = CascadeType.ALL)
    private List<TenantContextCategory> tenantCategories;
}
