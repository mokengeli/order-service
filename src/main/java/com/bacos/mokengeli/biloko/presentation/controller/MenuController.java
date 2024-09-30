package com.bacos.mokengeli.biloko.presentation.controller;

import com.bacos.mokengeli.biloko.application.domain.DomainMenu;
import com.bacos.mokengeli.biloko.application.service.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/menus")
public class MenuController {

    private final MenuService menuService;

    @Autowired
    public MenuController(MenuService menuService, @RequestHeader("Tenant-Code") String tenantCode) {
        this.menuService = menuService;
    }

    @PostMapping
    public DomainMenu saveMenu(DomainMenu menu, @RequestHeader("Tenant-Code") String tenantCode) {
        return menuService.createMenu(menu, tenantCode);
    }

    @GetMapping
    public List<DomainMenu> getAllMenus(@RequestHeader("Tenant-Code") String tenantCode) {
        return menuService.getAllMenus(tenantCode);
    }
}
