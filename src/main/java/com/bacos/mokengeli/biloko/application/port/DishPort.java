package com.bacos.mokengeli.biloko.application.port;

import com.bacos.mokengeli.biloko.application.domain.DomainDish;
import com.bacos.mokengeli.biloko.application.exception.ServiceException;

import java.util.List;
import java.util.Optional;

public interface DishPort {
    DomainDish createDish(DomainDish dish) throws ServiceException;
    Optional<List<DomainDish>> findAllDishesByTenant(String tenantCode);
    boolean isAllDishesOfTenant(String tenantCode, List<Long> dishIds);
    Optional<DomainDish> getDish(Long id) throws ServiceException;
    Double getDishPrice(Long dishId);
}
