package com.webstore.entity.order;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

import static com.webstore.constant.DatabaseConstants.SCHEMA_NAME;

@Entity
@Table(name = "order_status", schema = SCHEMA_NAME)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "order_status_seq")
    @SequenceGenerator(name = "order_status_seq", sequenceName = "web_store.seq_order_status_id",
            schema = "web_store", allocationSize = 1)
    @Column(name = "status_id")
    private Integer statusId;

    @NotBlank(message = "Status name cannot be blank")
    @Column(name = "status_name", nullable = false, length = 20, unique = true)
    private String statusName;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "status", fetch = FetchType.LAZY)
    private List<Order> orders;

    @OneToMany(mappedBy = "oldStatus", fetch = FetchType.LAZY)
    private List<OrderHistory> oldStatusHistories;

    @OneToMany(mappedBy = "newStatus", fetch = FetchType.LAZY)
    private List<OrderHistory> newStatusHistories;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}