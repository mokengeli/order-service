package com.bacos.mokengeli.biloko.presentation.model;

import lombok.Data;

import java.util.List;

@Data
public class CreateMenuRequest {
    private String name;
    private Double price;
    private String tenantCode;
    private Long currencyId;
    private List<Long> dishIds;  // List of dish IDs that are part of the menu
}
