package com.bacos.mokengeli.biloko.infrastructure.mapper;

import com.bacos.mokengeli.biloko.application.domain.DomainTenantContext;
import com.bacos.mokengeli.biloko.infrastructure.model.TenantContext;
import lombok.experimental.UtilityClass;

@UtilityClass
public class TenantContextMapper {

    public TenantContext toEntity(DomainTenantContext domainTenantContext) {
        if (domainTenantContext == null) {
            return null;
        }

        return TenantContext.builder()
                .id(domainTenantContext.getId())
                .tenantCode(domainTenantContext.getTenantCode())
                .tenantName(domainTenantContext.getTenantName())
                .build();
    }

    public DomainTenantContext toDomain(TenantContext tenantContext) {
        if (tenantContext == null) {
            return null;
        }

        return DomainTenantContext.builder()
                .id(tenantContext.getId())
                .tenantCode(tenantContext.getTenantCode())
                .tenantName(tenantContext.getTenantName())
                .build();
    }
}
