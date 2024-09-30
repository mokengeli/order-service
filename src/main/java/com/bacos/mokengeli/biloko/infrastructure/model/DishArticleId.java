package com.bacos.mokengeli.biloko.infrastructure.model;

import jakarta.persistence.Embeddable;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@Embeddable
public class DishArticleId implements Serializable {

    private Long dishId;
    private Long articleId;
}
