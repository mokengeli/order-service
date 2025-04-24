package com.bacos.mokengeli.biloko.infrastructure.mapper;


import com.bacos.mokengeli.biloko.application.domain.DomainMenu;
import com.bacos.mokengeli.biloko.application.domain.DomainRefTable;
import com.bacos.mokengeli.biloko.infrastructure.model.Menu;
import com.bacos.mokengeli.biloko.infrastructure.model.RefTable;
import lombok.experimental.UtilityClass;

@UtilityClass
public class RefTableMapper {


    public RefTable toEntity(DomainRefTable refTable) {
        if (refTable == null) {
            return null;
        }
        return RefTable.builder()
                .name(refTable.getName())
                .createdAt(refTable.getCreatedAt())
                .updatedAt(refTable.getUpdatedAt())
                .build();
    }

    public DomainRefTable toDomain(RefTable refTable) {
        if (refTable == null) {
            return null;
        }
        return DomainRefTable.builder()
                .name(refTable.getName())
                .tenantCode(refTable.getTenantContext().getTenantCode())
                .createdAt(refTable.getCreatedAt())
                .updatedAt(refTable.getUpdatedAt())
                .build();
    }
}
