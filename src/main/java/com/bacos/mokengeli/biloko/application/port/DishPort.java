package com.bacos.mokengeli.biloko.application.port;

import com.bacos.mokengeli.biloko.application.domain.DomainDish;
import com.bacos.mokengeli.biloko.application.exception.ServiceException;

import java.util.List;

public interface DishPort {
    DomainDish saveDish(DomainDish dish) throws ServiceException;

    List<DomainDish> findAllDishesByTenant(String tenantCode);  // Fetch dishes by tenant
}
