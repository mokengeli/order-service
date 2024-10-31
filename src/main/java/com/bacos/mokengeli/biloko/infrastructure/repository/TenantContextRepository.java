package com.bacos.mokengeli.biloko.infrastructure.repository;

import com.bacos.mokengeli.biloko.infrastructure.model.TenantContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TenantContextRepository extends JpaRepository<TenantContext, Long> {
    Optional<TenantContext> findByTenantCode(String tenantCode);

    boolean existsByTenantCode(String tenantCode);
}
