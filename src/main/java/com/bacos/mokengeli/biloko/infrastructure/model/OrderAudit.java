package com.bacos.mokengeli.biloko.infrastructure.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "orders_audit")
public class OrderAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne
    @JoinColumn(name = "tenant_context_id", nullable = false)
    private TenantContext tenantContext;

    @Column(name = "audit_action", nullable = false)
    private String auditAction;

    @Column(name = "old_state")
    private String oldState;

    @Column(name = "new_state")
    private String newState;

    @Column(name = "change_timestamp", nullable = false)
    private LocalDateTime changeTimestamp;

    @Column(name = "changed_by", nullable = false)
    private String changedBy;
}