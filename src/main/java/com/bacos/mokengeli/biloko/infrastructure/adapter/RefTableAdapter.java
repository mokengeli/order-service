package com.bacos.mokengeli.biloko.infrastructure.adapter;

import com.bacos.mokengeli.biloko.application.domain.DomainRefTable;
import com.bacos.mokengeli.biloko.application.port.RefTablePort;
import com.bacos.mokengeli.biloko.infrastructure.mapper.RefTableMapper;
import com.bacos.mokengeli.biloko.infrastructure.model.RefTable;
import com.bacos.mokengeli.biloko.infrastructure.repository.RefTableRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
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

    @Override
    public List<DomainRefTable> getRefTablesName(String name, String tenantCode) {
        List<RefTable> refTables = this.refTableRepository.findByNameContainingIgnoreCaseAndTenantCode(name, tenantCode);
        if (refTables == null || refTables.isEmpty()) {
            return new ArrayList<>();
        }
        return refTables.stream().map(RefTableMapper::toDomain).toList();
    }


}
