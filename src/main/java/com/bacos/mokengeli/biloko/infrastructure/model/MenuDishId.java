package com.bacos.mokengeli.biloko.infrastructure.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@Data
@NoArgsConstructor
public class MenuDishId implements Serializable {

    private Long menu; // Should match `menu` in `MenuDish` BE CAREFULL WHEN RENAMEMING
    private Long dish; // Should match `dish` in `MenuDish`

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MenuDishId that = (MenuDishId) o;
        return Objects.equals(menu, that.menu) &&
               Objects.equals(dish, that.dish);
    }

    @Override
    public int hashCode() {
        return Objects.hash(menu, dish);
    }
}
