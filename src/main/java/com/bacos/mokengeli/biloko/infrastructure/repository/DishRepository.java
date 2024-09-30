package com.bacos.mokengeli.biloko.infrastructure.repository;

import org.springframework.stereotype.Repository;

@Repository
public interface DishRepository extends JpaRepository<Dish, Long> {
    List<Dish> findAllByTenantCode(String tenantCode);
}
