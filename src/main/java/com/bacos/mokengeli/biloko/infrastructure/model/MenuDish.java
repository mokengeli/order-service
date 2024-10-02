package com.bacos.mokengeli.biloko.infrastructure.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "menu_dishes", schema = "order_service_schema")
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

}
