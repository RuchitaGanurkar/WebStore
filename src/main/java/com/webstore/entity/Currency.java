package com.webstore.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import lombok.Data;

@Data
@Entity
@Table(name = "currency", schema = "web_store")
public class Currency {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "currency_generator")
    @SequenceGenerator(name = "currency_generator", sequenceName = "web_store.seq_currency_id", allocationSize = 1)
    @Column(name = "currency_id")
    private Integer currencyId;

    @Column(name = "currency_code", length = 5, nullable = false, unique = true)
    private String currencyCode;

    @Column(name = "currency_name", length = 10, nullable = false)
    private String currencyName;

    @Column(name = "currency_symbol", length = 5, nullable = false)
    private String currencySymbol;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 50)
    private String createdBy;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", length = 50)
    private String updatedBy;
}