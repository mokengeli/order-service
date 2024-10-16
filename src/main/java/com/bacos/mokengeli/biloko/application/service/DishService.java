package com.bacos.mokengeli.biloko.application.service;

import com.bacos.mokengeli.biloko.application.domain.DomainDish;
import com.bacos.mokengeli.biloko.application.domain.model.ConnectedUser;
import com.bacos.mokengeli.biloko.application.exception.ServiceException;
import com.bacos.mokengeli.biloko.application.port.DishPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class DishService {

    private final DishPort dishPort;
    private final UserAppService userAppService;

    @Autowired
    public DishService(DishPort dishPort, UserAppService userAppService) {
        this.dishPort = dishPort;
        this.userAppService = userAppService;
    }

    public DomainDish createDish(DomainDish dish) throws ServiceException {
        // Assign tenant code
        ConnectedUser connectedUser = this.userAppService.getConnectedUser();

        if (Objects.isNull(dish.getName())) {
            String errorId = UUID.randomUUID().toString();
            log.error("[{}]: User [{}]. No name given for dish creation", errorId, connectedUser.getEmployeeNumber());
            throw new ServiceException(errorId, "Name of dish cannot be empty");
        }
        if (Objects.isNull(dish.getTenantCode())) {
            String errorId = UUID.randomUUID().toString();
            log.error("[{}]: User [{}]. No tenantCode given for dish creation", errorId, connectedUser.getEmployeeNumber());
            throw new ServiceException(errorId, "tenant is mandatory");
        }
        if (Objects.isNull(dish.getPrice())) {
            String errorId = UUID.randomUUID().toString();
            log.error("[{}]: User [{}]. No price given for dish creation", errorId, connectedUser.getEmployeeNumber());
            throw new ServiceException(errorId, "price is mandatory");
        }
        if (dish.getPrice() < 0) {
            String errorId = UUID.randomUUID().toString();
            log.error("[{}]: User [{}]. price = [{}] must be >= 0", errorId, connectedUser.getEmployeeNumber(), dish.getPrice());
        }

        if (Objects.isNull(dish.getCurrency().getId())) {
            String errorId = UUID.randomUUID().toString();
            log.error("[{}]: User [{}]. No currency given for dish creation", errorId, connectedUser.getEmployeeNumber());
            throw new ServiceException(errorId, "currency is mandatory");
        }

        if (!this.userAppService.isAdminUser()
                && !dish.getTenantCode().equals(connectedUser.getTenantCode())) {
            String errorId = UUID.randomUUID().toString();
            log.error("[{}]: User [{}] of tenant [{}] try to add dish of tenant [{}]", errorId, connectedUser.getEmployeeNumber(),
                    connectedUser.getTenantCode(), dish.getTenantCode());
            throw new ServiceException(errorId, "You can't add item owning by another partener");
        }
        try {
            return dishPort.createDish(dish);
        } catch (ServiceException e) {
            log.error("[{}]: User [{}]. {}", e.getTechnicalId(),
                    connectedUser.getEmployeeNumber(), e.getMessage());
            throw new ServiceException(e.getTechnicalId(), "Technical Error");
        }

    }

    public List<DomainDish> getAllDishes() throws ServiceException {
        ConnectedUser connectedUser = this.userAppService.getConnectedUser();
        Optional<List<DomainDish>> allDishesByTenant = dishPort.findAllDishesByTenant(connectedUser.getTenantCode());
        return allDishesByTenant.orElseGet(ArrayList::new);
    }

    public DomainDish getDish(Long id) throws ServiceException {
        ConnectedUser connectedUser = this.userAppService.getConnectedUser();
        String tenantCode = connectedUser.getTenantCode();
        List<Long> ids = Collections.singletonList(id);
        boolean allDishesOfTenant = this.dishPort.isAllDishesOfTenant(tenantCode, ids);
        if (!allDishesOfTenant) {
            String errorId = UUID.randomUUID().toString();
            log.error("[{}]: User [{}] of tenant [{}] try to get a dish of different tenant. Here it's the dish ids [{}]", errorId, connectedUser.getEmployeeNumber(),
                    connectedUser.getTenantCode(), ids);
        }
        Optional<DomainDish> optDish = this.dishPort.getDish(id);
        if (optDish.isPresent()) {
            return optDish.get();
        }
        throw new ServiceException(UUID.randomUUID().toString(), "Dish not found");
    }
}
