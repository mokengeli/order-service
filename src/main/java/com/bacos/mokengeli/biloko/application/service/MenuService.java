package com.bacos.mokengeli.biloko.application.service;


import com.bacos.mokengeli.biloko.application.domain.DomainMenu;
import com.bacos.mokengeli.biloko.application.port.MenuPort;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MenuService {

    private final MenuPort menuPort;

    @Autowired
    public MenuService(MenuPort menuPort) {
        this.menuPort = menuPort;
    }

    // Method to create a menu
    public DomainMenu createMenu(DomainMenu request, String tenantCode) {
        DomainMenu menu = new DomainMenu();
        menu.setName(request.getName());
        menu.setDishes(request.getDishes());
        menu.setPrice(request.getPrice());
        menu.setTenantCode(tenantCode);  // Assign tenant code for multi-tenancy
        return menuPort.saveMenu(menu);
    }

    // Method to get all menus by tenant
    public List<DomainMenu> getAllMenus(String tenantCode) {
        return menuPort.findAllMenusByTenant(tenantCode);
    }
}
