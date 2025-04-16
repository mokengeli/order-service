package com.bacos.mokengeli.biloko.application.service;

import com.bacos.mokengeli.biloko.application.domain.DomainCategory;
import com.bacos.mokengeli.biloko.application.domain.model.ConnectedUser;
import com.bacos.mokengeli.biloko.application.exception.ServiceException;
import com.bacos.mokengeli.biloko.application.port.CategoryPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

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

    public List<DomainCategory> getAllCategories(String tenantCode) throws ServiceException {
        ConnectedUser connectedUser = this.userAppService.getConnectedUser();
        if (!this.userAppService.isAdminUser() &&
                !connectedUser.getTenantCode().equals(tenantCode)) {
            String uuid = UUID.randomUUID().toString();
            log.error("[{}]: User [{}] Tenant [{}] try to get categories of another tenant: {}", uuid,
                    connectedUser.getEmployeeNumber(), connectedUser.getTenantCode(), tenantCode);

            throw new ServiceException(uuid, "An internal error occurred");
        }
        try {
            return categoryPort.getAllCategoriesOfTenant(tenantCode);

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
