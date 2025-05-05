package com.bacos.mokengeli.biloko.application.domain.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO métier pour un plat "top" : id, nom, quantité et CA généré.
 */
@Data
@AllArgsConstructor
public class DomainTopDish {

    private Long dishId;
    private String name;
    private Long quantity;
    private Double revenue;
}