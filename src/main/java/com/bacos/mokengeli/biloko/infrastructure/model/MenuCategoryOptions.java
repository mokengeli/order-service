package com.bacos.mokengeli.biloko.infrastructure.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Entity
@Table(name = "menu_category_options")
@IdClass(MenuCategoryOptionsId.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuCategoryOptions implements Serializable {

    @Id
    @ManyToOne
    @JoinColumn(name = "menu_id", nullable = false)
    private Menu menu;

    @Id
    @Column(name = "category", nullable = false)
    private String category;

    @Column(name = "max_choices", nullable = false)
    private int maxChoices;
}
