package com.bacos.mokengeli.biloko.application.port;

import com.bacos.mokengeli.biloko.application.domain.DomainMenu;

import java.util.List;

public interface MenuPort {
    DomainMenu saveMenu(DomainMenu menu);

    List<DomainMenu> findAllMenusByTenant(String tenantCode);  // Fetch menus by tenant
}
