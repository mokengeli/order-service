package com.bacos.mokengeli.biloko.presentation.model;

import lombok.Data;
import java.util.List;

@Data
public class CreateDishRequest {
    private String name;
    private Double price;
    private String tenantCode;
    private List<DishProductRequest> dishProducts;
    private List<String> categories;
    private Long currencyId;

    @Data
    public static class DishProductRequest {
        private Long productId;
        private Double quantity;
        private Boolean removable;
    }
}
