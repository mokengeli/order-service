package com.bacos.mokengeli.biloko.application.service;

import com.bacos.mokengeli.biloko.application.domain.DomainCategory;
import com.bacos.mokengeli.biloko.application.domain.model.ConnectedUser;
import com.bacos.mokengeli.biloko.application.exception.ServiceException;
import com.bacos.mokengeli.biloko.application.port.CategoryPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
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

    public Page<DomainCategory> getAllCategories(
            String tenantCode,
            int    page,
            int    size,
            String search
    ) throws ServiceException {
        ConnectedUser connectedUser = this.userAppService.getConnectedUser();
        if (!this.userAppService.isAdminUser() &&
                !connectedUser.getTenantCode().equals(tenantCode)) {
            String uuid = UUID.randomUUID().toString();
            log.error("[{}]: User [{}] Tenant [{}] tried to search categories of another tenant: {}",
                    uuid, connectedUser.getEmployeeNumber(), connectedUser.getTenantCode(), tenantCode);
            throw new ServiceException(uuid, "You don't have permission to get Categories");
        }
        try {
            return categoryPort.getAllCategoriesOfTenant(tenantCode, page, size, search);
        } catch (ServiceException e) {
            log.error("[{}]: User [{}]. message: {}", e.getTechnicalId(),
                    connectedUser.getEmployeeNumber(), e.getMessage());
            throw new ServiceException(e.getTechnicalId(), "An internal error occurred");
        }
    }

    public Page<DomainCategory> getAllCategories(int page, int size,  String search ) throws ServiceException {
        return categoryPort.getAllCategories(page, size, search);
    }


    public DomainCategory createCategory(DomainCategory category) throws ServiceException {
        ConnectedUser connectedUser = this.userAppService.getConnectedUser();
        if (!this.userAppService.isAdminUser()) {
            String uuid = UUID.randomUUID().toString();
            log.error("[{}]: User [{}] Tenant [{}] try to create category {} but don't have permission", uuid,
                    connectedUser.getEmployeeNumber(), connectedUser.getTenantCode(), category.getName());

            throw new ServiceException(uuid, "You don't have permission to create Category");
        }
        try {
            return categoryPort.addCategory(category);
        } catch (DataIntegrityViolationException e) {
            String uuid = UUID.randomUUID().toString();
            log.error("[{}]: User[{}] An error Occured. The Category already exist", uuid,
                    connectedUser.getEmployeeNumber());
            throw new ServiceException(uuid, "An error Occured. The Category already exist");
        }

    }

    public void assignCategoryToTenant(String tenantCode, Long categoryId) throws ServiceException {
        ConnectedUser connectedUser = this.userAppService.getConnectedUser();
        if (!this.userAppService.isAdminUser() &&
                !connectedUser.getTenantCode().equals(tenantCode)) {
            String uuid = UUID.randomUUID().toString();
            log.error("[{}]: User [{}] Tenant [{}] try to assign category {} to another tenant: {}", uuid,
                    connectedUser.getEmployeeNumber(), connectedUser.getTenantCode(), categoryId, tenantCode);

            throw new ServiceException(uuid, "You don't have permission to perfom this action");
        }
        this.categoryPort.assiginToTenant(categoryId, tenantCode);
    }
}
