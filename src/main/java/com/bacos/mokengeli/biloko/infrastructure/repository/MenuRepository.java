package com.bacos.mokengeli.biloko.infrastructure.repository;

import org.springframework.stereotype.Repository;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Long> {
    List<Menu> findAllByTenantCode(String tenantCode);
}
