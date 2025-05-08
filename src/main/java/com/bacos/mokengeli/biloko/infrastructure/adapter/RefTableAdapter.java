package com.bacos.mokengeli.biloko.infrastructure.adapter;

import com.bacos.mokengeli.biloko.application.domain.DomainRefTable;
import com.bacos.mokengeli.biloko.application.port.RefTablePort;
import com.bacos.mokengeli.biloko.infrastructure.mapper.RefTableMapper;
import com.bacos.mokengeli.biloko.infrastructure.repository.RefTableRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class RefTableAdapter implements RefTablePort {


    private final RefTableRepository refTableRepository;

    public RefTableAdapter(RefTableRepository refTableRepository) {
        this.refTableRepository = refTableRepository;
    }

    @Override
    public Optional<DomainRefTable> getRefTableById(Long id, String tenantCode) {
        return refTableRepository
                .findByIdAndTenantCode(id, tenantCode)  // filtre multi-tenant
                .map(RefTableMapper::toDomain);
    }

    @Override
    public long countRefTablesByTenantCode(String tenantCode) {
        return refTableRepository.countByTenantCode(tenantCode);
    }


}
