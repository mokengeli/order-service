package com.bacos.mokengeli.biloko.infrastructure.model;

import com.bacos.mokengeli.biloko.application.domain.MenuCategory;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "menu_dishes")
@IdClass(MenuDishId.class)
public class MenuDish implements Serializable {

    @Id
    @ManyToOne
    @JoinColumn(name = "menu_id", nullable = false)
    private Menu menu;

    @Id
    @ManyToOne
    @JoinColumn(name = "dish_id", nullable = false)
    private Dish dish;

    @Column(name = "category", nullable = false)
    @Enumerated(EnumType.STRING)
    private MenuCategory category;

}
