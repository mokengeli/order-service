package com.bacos.mokengeli.biloko.application.domain.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DomainDishCategoryStat {
    private String categoryName;
    private Long   value;
}