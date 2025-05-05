package com.bacos.mokengeli.biloko.application.domain.dashboard;


import com.bacos.mokengeli.biloko.application.domain.DomainCurrency;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class DomainRevenueDashboard {
    /**
     * CA real correspondant au montant réellement percus pour les commandes
     */
    private double realRevenue;
    /**
     * CA theorique c'est a dire correspondant a la somme des prix commandes
     */
    private double theoreticalRevenue;
    private DomainCurrency currency;
    private List<DomainOrderDashboard> orders;
    private DomainBreakdown breakdown;
}
