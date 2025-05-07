package com.bacos.mokengeli.biloko.infrastructure.mapper;

import com.bacos.mokengeli.biloko.application.domain.DomainTenant;
import com.bacos.mokengeli.biloko.infrastructure.model.Tenant;
import lombok.experimental.UtilityClass;

@UtilityClass
public class TenantMapper {


    public Tenant toEntity(DomainTenant domainTenant) {
        Tenant tenant = new Tenant();
        tenant.setName(domainTenant.getName());
        tenant.setCode(domainTenant.getCode());
        return tenant;
    }
}
