package com.bacos.mokengeli.biloko.application.domain;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DomainPromotion {
    private Long id;
    private Long tenantId;
    private Long dishId;  // Optional if linked to dish
    private Long menuId;  // Optional if linked to menu
    private Double discountPercentage;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean isActive;
}
