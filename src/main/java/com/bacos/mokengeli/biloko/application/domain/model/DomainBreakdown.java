package com.bacos.mokengeli.biloko.application.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DomainBreakdown {
    /**
     * total CA des paiement sans discount, rejets de certains items etc
     */
    private double fullPayments;
    /**
     * total CA dans les cas de discount, rejets de certains items, etc
     */
    private double discountedPayments;
}

