package com.bacos.mokengeli.biloko.application.domain;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class DomainTenantContext {
    private Long id;
    private String tenantCode;
    private String tenantName;
    private List<DomainDish> dishes;
    private List<DomainMenu> menus;
    private List<DomainArticle> articles;
}
