package com.bacos.mokengeli.biloko.presentation.model;

import lombok.Data;

import java.util.List;

@Data
public class CreateMenuRequest {
    private String name;
    private Double price;
    private String tenantCode;
    private Long currencyId;
    private List<CompositionMenuRequest> compositions;

    @Data
    public static class CompositionMenuRequest {
        private List<Long> dishIds;  // List of dish IDs that are part of the menu
        private String category;
        private int maxChoice;
    }
}
