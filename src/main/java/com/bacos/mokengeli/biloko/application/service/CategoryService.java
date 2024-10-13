package com.bacos.mokengeli.biloko.application.service;

import com.bacos.mokengeli.biloko.application.domain.DomainCategory;
import com.bacos.mokengeli.biloko.application.domain.model.ConnectedUser;
import com.bacos.mokengeli.biloko.application.port.CategoryPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryPort categoryPort;
    private final UserAppService userAppService;

    @Autowired
    public CategoryService(CategoryPort categoryPort, UserAppService userAppService) {
        this.categoryPort = categoryPort;
        this.userAppService = userAppService;
    }

    public List<DomainCategory> getAllCategories() {
        ConnectedUser connectedUser = this.userAppService.getConnectedUser();
        return categoryPort.getAllCategories(connectedUser.getTenantCode());
    }

    public DomainCategory createCategory(DomainCategory category) {
        return categoryPort.addCategory(category);
    }
}
