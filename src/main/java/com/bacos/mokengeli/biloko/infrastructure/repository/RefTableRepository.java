package com.bacos.mokengeli.biloko.infrastructure.repository;

import com.bacos.mokengeli.biloko.infrastructure.model.RefTable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefTableRepository extends JpaRepository<RefTable, Long> {
    boolean existsByNameAndTenantCode(String name, String code);

    Page<RefTable> findByTenantCode(String tenantCode, Pageable pageable);

    boolean existsByIdAndTenantCode(Long id, String code);
}
