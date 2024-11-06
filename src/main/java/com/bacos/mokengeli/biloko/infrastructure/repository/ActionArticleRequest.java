package com.bacos.mokengeli.biloko.infrastructure.repository;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ActionArticleRequest {
    private Long productId;
    public  double quantity;
}
