package com.bacos.mokengeli.biloko.presentation.model;

import lombok.Data;
import java.util.List;

@Data
public class CreateDishRequest {
    private String name;
    private Double price;
    private String tenantCode;
    private List<DishArticleRequest> dishArticles;

    @Data
    public static class DishArticleRequest {
        private Long articleId;
        private Double quantity;
        private Boolean removable;
    }
}
