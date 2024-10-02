package com.bacos.mokengeli.biloko.application.service;

import com.bacos.mokengeli.biloko.application.domain.DomainDish;
import com.bacos.mokengeli.biloko.application.domain.model.ConnectedUser;
import com.bacos.mokengeli.biloko.application.exception.ServiceException;
import com.bacos.mokengeli.biloko.application.port.DishPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

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
        if (Objects.isNull(dish.getCurrentPrice())) {
            String errorId = UUID.randomUUID().toString();
            log.error("[{}]: User [{}]. No price given for dish creation", errorId, connectedUser.getEmployeeNumber());
            throw new ServiceException(errorId, "price is mandatory");
        }
        if (dish.getCurrentPrice() < 0) {
            String errorId = UUID.randomUUID().toString();
            log.error("[{}]: User [{}]. price = [{}] must be >= 0", errorId, connectedUser.getEmployeeNumber(), dish.getCurrentPrice());
        }

        if (!this.userAppService.isAdminUser()
                && !dish.getTenantCode().equals(connectedUser.getTenantCode())) {
            String errorId = UUID.randomUUID().toString();
            log.error("[{}]: User [{}] of tenant [{}] try to add dish of tenant [{}]", errorId, connectedUser.getEmployeeNumber(),
                    connectedUser.getTenantCode(), dish.getTenantCode());
            throw new ServiceException(errorId, "You can't add item owning by another partener");
        }

        return dishPort.saveDish(dish);
    }

    public List<DomainDish> getAllDishes(String tenantCode) throws ServiceException {
        ConnectedUser connectedUser = this.userAppService.getConnectedUser();
        if (!this.userAppService.isAdminUser()
                && !tenantCode.equals(connectedUser.getTenantCode())) {
            String errorId = UUID.randomUUID().toString();
            log.error("[{}]: User [{}] of tenant [{}] try to add dish of tenant [{}]", errorId, connectedUser.getEmployeeNumber(),
                    connectedUser.getTenantCode(), tenantCode);
            throw new ServiceException(errorId, "You can't add item owning by another partener");
        }
        return dishPort.findAllDishesByTenant(tenantCode);
    }
}
