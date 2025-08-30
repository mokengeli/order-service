package com.bacos.mokengeli.biloko.infrastructure.mapper;


import com.bacos.mokengeli.biloko.application.domain.DomainRefTable;
import com.bacos.mokengeli.biloko.infrastructure.model.RefTable;
import lombok.experimental.UtilityClass;

@UtilityClass
public class RefTableMapper {


    public RefTable toEntity(DomainRefTable refTable) {
        if (refTable == null) {
            return null;
        }
        return RefTable.builder()
                .name(refTable.getName().toUpperCase())
                .createdAt(refTable.getCreatedAt())
                .updatedAt(refTable.getUpdatedAt())
                .build();
    }

    public DomainRefTable toDomain(RefTable refTable) {
        if (refTable == null) {
            return null;
        }
        return DomainRefTable.builder()
                .id(refTable.getId())
                .name(refTable.getName())
                .tenantCode(refTable.getTenant().getCode())
                .createdAt(refTable.getCreatedAt())
                .updatedAt(refTable.getUpdatedAt())
                .build();
    }
}
