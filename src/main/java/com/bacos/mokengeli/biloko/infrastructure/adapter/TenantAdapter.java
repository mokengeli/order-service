package com.bacos.mokengeli.biloko.infrastructure.adapter;

import com.bacos.mokengeli.biloko.application.domain.DomainTenant;
import com.bacos.mokengeli.biloko.application.exception.ServiceException;
import com.bacos.mokengeli.biloko.application.service.TenantPort;
import com.bacos.mokengeli.biloko.infrastructure.repository.TenantRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;


@Component
public class TenantAdapter implements TenantPort {
    private final TenantRepository tenantRepository;

    public TenantAdapter(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    @Override
    public String getTenantName(String tenantCode) throws ServiceException {
        Optional<String> nameByCode = this.tenantRepository.findNameByCode(tenantCode);
        if (nameByCode.isEmpty()) {
            throw new ServiceException(UUID.randomUUID().toString(),
                    "No tenant  find with tenant_code=" + tenantCode);
        }
        return nameByCode.get();
    }
}
