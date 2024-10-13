package com.bacos.mokengeli.biloko.application.port;

import com.bacos.mokengeli.biloko.application.domain.DomainCategory;

import java.util.List;

public interface CategoryPort {
    List<DomainCategory> getAllCategories(String tenantCode);
    DomainCategory addCategory(DomainCategory category);
}
