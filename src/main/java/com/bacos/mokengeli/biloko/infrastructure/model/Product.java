package com.bacos.mokengeli.biloko.infrastructure.model;


import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Product {

    private Long id;
    private String name;
    private String unitOfMeasure;

}
