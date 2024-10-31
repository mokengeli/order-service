package com.bacos.mokengeli.biloko.application.service;

import com.bacos.mokengeli.biloko.application.domain.DomainCategory;
import com.bacos.mokengeli.biloko.application.domain.model.ConnectedUser;
import com.bacos.mokengeli.biloko.application.exception.ServiceException;
import com.bacos.mokengeli.biloko.application.port.CategoryPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class CategoryService {

    private final CategoryPort categoryPort;
    private final UserAppService userAppService;

    @Autowired
    public CategoryService(CategoryPort categoryPort, UserAppService userAppService) {
        this.categoryPort = categoryPort;
        this.userAppService = userAppService;
    }

    public List<DomainCategory> getAllCategories() throws ServiceException {
        ConnectedUser connectedUser = this.userAppService.getConnectedUser();

        try {
            return categoryPort.getAllCategoriesOfTenant(connectedUser.getTenantCode());

        } catch (ServiceException e) {
            log.error("[{}]: User [{}]. message: {}", e.getTechnicalId(),
                    connectedUser.getEmployeeNumber(), e.getMessage());
            throw new ServiceException(e.getTechnicalId(), "An internal error occurred");
        }
    }

    public DomainCategory createCategory(DomainCategory category) {
        return categoryPort.addCategory(category);
    }

}
