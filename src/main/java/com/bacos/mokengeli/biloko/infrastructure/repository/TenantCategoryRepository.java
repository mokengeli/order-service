package com.bacos.mokengeli.biloko.infrastructure.repository;

import com.bacos.mokengeli.biloko.infrastructure.model.TenantContextCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TenantCategoryRepository extends JpaRepository<TenantContextCategory, Long> {
    Optional<List<TenantContextCategory>> findByTenantContextTenantCode(String tenantCode);

}
