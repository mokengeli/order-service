package com.bacos.mokengeli.biloko.infrastructure.model;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuCategoryOptionsId implements Serializable {


    private long menu; // Should match `menu` in `MenuCategoryOptions`
    private String category;
}
