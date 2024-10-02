package com.bacos.mokengeli.biloko.application.service;


import com.bacos.mokengeli.biloko.application.domain.DomainDish;
import com.bacos.mokengeli.biloko.application.domain.DomainMenu;
import com.bacos.mokengeli.biloko.application.domain.model.ConnectedUser;
import com.bacos.mokengeli.biloko.application.exception.ServiceException;
import com.bacos.mokengeli.biloko.application.port.DishPort;
import com.bacos.mokengeli.biloko.application.port.MenuPort;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class MenuService {

    private final MenuPort menuPort;
    private final UserAppService userAppService;
    private final DishPort dishPort;

    @Autowired
    public MenuService(MenuPort menuPort, UserAppService userAppService, DishPort dishPort) {
        this.menuPort = menuPort;
        this.userAppService = userAppService;
        this.dishPort = dishPort;
    }

    public DomainMenu createMenu(DomainMenu menu) throws ServiceException {
        ConnectedUser connectedUser = this.userAppService.getConnectedUser();
        if (!this.userAppService.isAdminUser()
                && !menu.getTenantCode().equals(connectedUser.getTenantCode())) {
            String errorId = UUID.randomUUID().toString();
            log.error("[{}]: User [{}] of tenant [{}] try to add menu of tenant [{}]", errorId, connectedUser.getEmployeeNumber(),
                    connectedUser.getTenantCode(), menu.getTenantCode());
            throw new ServiceException(errorId, "You can't add item owning by another partner");
        }
        List<Long> dishIds = menu.getDishes().stream().map(DomainDish::getId).toList();
        boolean allDishesOfTenant = this.dishPort.isAllDishesOfTenant(menu.getTenantCode(), dishIds);
        if (!allDishesOfTenant) {
            String errorId = UUID.randomUUID().toString();
            log.error("[{}]: User [{}] of tenant [{}] try to add in menu dish of different tenant. Here it's the dish ids [{}]", errorId, connectedUser.getEmployeeNumber(),
                    connectedUser.getTenantCode(), dishIds);
        }
        try {
            return menuPort.createMenu(menu);
        } catch (ServiceException e) {
            log.error("[{}]: User [{}]. {}", e.getTechnicalId(),
                    connectedUser.getEmployeeNumber(), e.getMessage());
            throw new ServiceException(e.getTechnicalId(), "Technical Error");
        }
    }

    public List<DomainMenu> getAllMenus() {
        ConnectedUser connectedUser = this.userAppService.getConnectedUser();
        String tenantCode = connectedUser.getTenantCode();
        Optional<List<DomainMenu>> optDishes = this.menuPort.findAllMenusByTenant(tenantCode);
        return optDishes.orElseGet(ArrayList::new);
    }
}
