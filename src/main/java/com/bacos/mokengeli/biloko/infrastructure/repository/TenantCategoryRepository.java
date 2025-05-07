package com.bacos.mokengeli.biloko.infrastructure.repository;

import com.bacos.mokengeli.biloko.infrastructure.model.TenantCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantCategoryRepository extends JpaRepository<TenantCategory, Long> {
    Page<TenantCategory> findByTenantCode(String tenantCode, Pageable pageable);

}
