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

}
