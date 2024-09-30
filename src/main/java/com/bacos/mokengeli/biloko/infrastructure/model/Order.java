package com.bacos.mokengeli.biloko.infrastructure.model;

import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_number", nullable = false)
    private String employeeNumber;

    @Column(name = "table_number")
    private String tableNumber;

    @Column(name = "state", nullable = false)
    private String state;

    @Column(name = "total_price", nullable = false)
    private Double totalPrice;

    @Column(name = "comment")
    private String comment;

    @Column(name = "tenant_code", nullable = false)
    private String tenantCode;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems;
}
