package com.bacos.mokengeli.biloko.application.port;

import com.bacos.mokengeli.biloko.application.domain.DomainDish;
import com.bacos.mokengeli.biloko.application.exception.ServiceException;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

public interface DishPort {
    DomainDish createDish(DomainDish dish) throws ServiceException;
    Page<DomainDish> findAllDishesByTenant(String tenantCode, int page, int size) throws ServiceException;
    boolean isAllDishesOfTenant(String tenantCode, List<Long> dishIds);
    Optional<DomainDish> getDish(Long id) throws ServiceException;
    Double getDishPrice(Long dishId);

    boolean checkIfProductIsOk(String tenantCode, List<Long> productIds);
}
