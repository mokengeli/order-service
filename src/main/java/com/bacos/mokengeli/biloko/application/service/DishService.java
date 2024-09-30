package com.bacos.mokengeli.biloko.application.service;

import com.bacos.mokengeli.biloko.application.domain.DomainDish;
import com.bacos.mokengeli.biloko.application.port.DishPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DishService {

    private final DishPort dishPort;

    @Autowired
    public DishService(DishPort dishPort) {
        this.dishPort = dishPort;
    }

    public DomainDish createDish(DomainDish request, String tenantCode) {
        DomainDish dish = new DomainDish();
        dish.setName(request.getName());
        dish.setPrice(request.getPrice());
        dish.setArticles(request.getArticles());
        dish.setTenantCode(tenantCode);  // Assign tenant code
        return dishPort.saveDish(dish);
    }

    public List<DomainDish> getAllDishes(String tenantCode) {
        return dishPort.findAllDishesByTenant(tenantCode);  // Get dishes by tenant
    }
}
