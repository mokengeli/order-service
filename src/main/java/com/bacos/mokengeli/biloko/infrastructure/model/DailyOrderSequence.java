package com.bacos.mokengeli.biloko.infrastructure.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "daily_order_sequence")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyOrderSequence {

    @Id
    @Column(name = "id", length = 60)
    private String id;

    @Column(name = "tenant_code", length = 50, nullable = false)
    private String tenantCode;

    @Column(name = "business_date", nullable = false)
    private LocalDate businessDate;

    @Column(name = "current_sequence", nullable = false)
    private Integer currentSequence = 0;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        OffsetDateTime now = OffsetDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        
        // Génère l'ID automatiquement si pas défini
        if (this.id == null) {
            this.id = generateId();
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    private String generateId() {
        return String.format("%s_%s", tenantCode, businessDate.toString());
    }

    public void incrementSequence() {
        this.currentSequence++;
    }
}