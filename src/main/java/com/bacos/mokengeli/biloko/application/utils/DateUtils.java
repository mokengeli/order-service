package com.bacos.mokengeli.biloko.application.utils;

import lombok.experimental.UtilityClass;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@UtilityClass
public class DateUtils {

    // Récupérer la timezone du système (configurée dans Application.java)
    private final ZoneId systemZone = ZoneId.systemDefault();

    /**
     * Convertit un LocalDateTime en OffsetDateTime en utilisant la timezone du système
     */
    public OffsetDateTime toOffsetDateTime(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }

        // Méthode 1 : Utiliser ZoneId (recommandé - gère les changements d'heure été/hiver)
        return localDateTime.atZone(systemZone).toOffsetDateTime();
    }

    /**
     * Obtient le début du jour en OffsetDateTime
     */
    public OffsetDateTime startOfDay(LocalDate date) {
        return toOffsetDateTime(date.atStartOfDay());
    }

    /**
     * Obtient la fin du jour en OffsetDateTime
     */
    public OffsetDateTime endOfDay(LocalDate date) {
        return toOffsetDateTime(date.atTime(23, 59, 59, 999_999_999));
    }

    /**
     * Calcule la date métier actuelle (reset à 6h du matin)
     * Pour les restaurants 24h/24, le "jour métier" commence à 6h du matin
     */
    public LocalDate getCurrentBusinessDate() {
        LocalDateTime now = LocalDateTime.now(systemZone);
        
        // Si il est avant 6h du matin, on considère qu'on est encore sur le jour précédent
        if (now.getHour() < 6) {
            return now.toLocalDate().minusDays(1);
        }
        
        return now.toLocalDate();
    }

    /**
     * Obtient la timezone système
     */
    public ZoneId getSystemZone() {
        return systemZone;
    }

    /**
     * Convertit une date métier (LocalDate) en plage OffsetDateTime pour les requêtes
     * Utile pour filtrer les commandes sur une date métier spécifique
     */
    public OffsetDateTime[] getBusinessDateRange(LocalDate businessDate) {
        // Une date métier va de 6h ce jour-là à 6h le lendemain
        OffsetDateTime start = toOffsetDateTime(businessDate.atTime(6, 0, 0));
        OffsetDateTime end = toOffsetDateTime(businessDate.plusDays(1).atTime(5, 59, 59, 999_999_999));
        
        return new OffsetDateTime[]{start, end};
    }

}
