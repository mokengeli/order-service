package com.bacos.mokengeli.biloko.infrastructure.adapter;

import com.bacos.mokengeli.biloko.application.domain.DomainMenu;
import com.bacos.mokengeli.biloko.application.port.MenuPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MenuAdapter implements MenuPort {

    private final MenuRepository menuRepository;

    @Autowired
    public MenuAdapter(MenuRepository menuRepository) {
        this.menuRepository = menuRepository;
    }

    @Override
    public DomainMenu saveMenu(DomainMenu menu) {
        return menuRepository.save(menu);
    }

    @Override
    public List<DomainMenu> findAllMenusByTenant(String tenantCode) {
        return menuRepository.findAllByTenantCode(tenantCode);  // Fetch by tenant code
    }
}
