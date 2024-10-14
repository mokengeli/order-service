package com.bacos.mokengeli.biloko.infrastructure.repository;

import com.bacos.mokengeli.biloko.infrastructure.model.TenantCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TenantCategoryRepository extends JpaRepository<TenantCategory, Long> {
    Optional<TenantCategory> findByTenantCode(String tenantCode);

}
