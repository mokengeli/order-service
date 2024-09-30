package com.bacos.mokengeli.biloko.infrastructure.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "dish_articles")
public class DishArticle {

    @EmbeddedId
    private DishArticleId id;

    @ManyToOne
    @MapsId("dishId")
    @JoinColumn(name = "dish_id")
    private Dish dish;

    @ManyToOne
    @MapsId("articleId")
    @JoinColumn(name = "article_id")
    private Article article;

    @Column(name = "quantity", nullable = false)
    private Double quantity;

    @Column(name = "removable", nullable = false)
    private Boolean removable = true;
}
