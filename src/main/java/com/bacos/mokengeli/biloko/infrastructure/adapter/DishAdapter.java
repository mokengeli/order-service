package com.bacos.mokengeli.biloko.infrastructure.adapter;

import com.bacos.mokengeli.biloko.application.domain.DomainDish;
import com.bacos.mokengeli.biloko.application.port.DishPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DishAdapter implements DishPort {

    private final DishRepository dishRepository;

    @Autowired
    public DishAdapter(DishRepository dishRepository) {
        this.dishRepository = dishRepository;
    }

    @Override
    public DomainDish saveDish(DomainDish dish) {
        return dishRepository.save(dish);
    }

    @Override
    public List<DomainDish> findAllDishesByTenant(String tenantCode) {
        return dishRepository.findAllByTenantCode(tenantCode);  // Fetch by tenant code
    }
}
