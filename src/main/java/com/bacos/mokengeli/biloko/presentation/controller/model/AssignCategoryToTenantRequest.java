package com.bacos.mokengeli.biloko.presentation.controller.model;


import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class AssignCategoryToTenantRequest {
    private Long categoryId;
    private String tenantCode;
}
