package com.bacos.mokengeli.biloko.application.port;

import com.bacos.mokengeli.biloko.application.domain.DomainRefTable;

import java.util.List;
import java.util.Optional;

public interface RefTablePort {

    Optional<DomainRefTable> getRefTableById(Long id, String tenantCode);

    long countRefTablesByTenantCode(String tenantCode);

    List<DomainRefTable> getRefTablesName(String name, String tenantCode);
}
