package com.bacos.mokengeli.biloko.infrastructure.adapter;

import com.bacos.mokengeli.biloko.application.exception.ServiceException;
import com.bacos.mokengeli.biloko.infrastructure.model.TenantCategory;
import com.bacos.mokengeli.biloko.infrastructure.repository.TenantCategoryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
public class TenantCategoryAdapter {
    private final TenantCategoryRepository TenantCategoryRepository;
    private final ObjectMapper objectMapper;

    public TenantCategoryAdapter(TenantCategoryRepository TenantCategoryRepository, ObjectMapper objectMapper) {
        this.TenantCategoryRepository = TenantCategoryRepository;
        this.objectMapper = objectMapper;
    }

    // Method to add a category
    public void addCategory(String tenantCode, String category) throws ServiceException {
        TenantCategory config = TenantCategoryRepository.findByTenantCode(tenantCode)
                .orElseThrow(() -> new ServiceException(UUID.randomUUID().toString(),
                        "Tenant not found"));

        // Convert existing JSON to a Set
        Set<String> categories = getCategorySet(config);

        // Add the new category
        categories.add(category);

        // Update the JSONB field
        updateEnableCategory(config, categories);
    }

    // Method to remove a category
    public void removeCategory(String tenantCode, String category) throws ServiceException {
        TenantCategory config = TenantCategoryRepository.findByTenantCode(tenantCode)
                .orElseThrow(() -> new ServiceException(UUID.randomUUID().toString(), "Tenant not found"));

        // Convert existing JSON to a Set
        Set<String> categories = getCategorySet(config);

        // Remove the category
        categories.remove(category);

        // Update the JSONB field
        updateEnableCategory(config, categories);
    }

    public Optional<TenantCategory> getCategories(String tenantCode) {
        return TenantCategoryRepository.findByTenantCode(tenantCode);
    }

    // Helper method to convert JSONB to a Set<String>
    public Set<String> getCategorySet(TenantCategory config) throws ServiceException {
        try {
            List<String> categoryList = objectMapper.readValue(config.getEnabledCategory(), List.class);
            return new HashSet<>(categoryList);
        } catch (Exception e) {
            log.error("Failed to parse enable_category JSON", e);
            throw new ServiceException(UUID.randomUUID().toString(), "Failed to parse enable_category JSON");
        }
    }

    // Helper method to update the JSONB field
    private void updateEnableCategory(TenantCategory config, Set<String> categories) throws ServiceException {
        try {
            String updatedJson = objectMapper.writeValueAsString(categories);
            config.setEnabledCategory(updatedJson);
            TenantCategoryRepository.save(config);
        } catch (Exception e) {
            log.error("Failed to update enable_category", e);
            throw new ServiceException(UUID.randomUUID().toString(), "Failed to update enable_category JSON");
        }
    }
}
