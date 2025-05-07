package com.bacos.mokengeli.biloko.infrastructure.adapter;

import com.bacos.mokengeli.biloko.infrastructure.repository.TenantCategoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TenantCategoryAdapter {
    private final TenantCategoryRepository tenantCategoryRepository;


    public TenantCategoryAdapter(TenantCategoryRepository TenantCategoryRepository) {
        this.tenantCategoryRepository = TenantCategoryRepository;

    }


}
