package com.bacos.mokengeli.biloko.infrastructure.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class DishProductId implements Serializable {

    private Long dish;
    private Long product;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DishProductId that = (DishProductId) o;
        return Objects.equals(dish, that.dish) &&
                Objects.equals(product, that.product);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dish, product);
    }
}
