package com.webstore.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import lombok.Data;

@Data
@Entity
@Table(
        name = "catalogue_category",
        schema = "web_store",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_catalogue_category",
                columnNames = {"catalogue_id", "category_id"}
        )
)
public class CatalogueCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "catalogue_category_generator")
    @SequenceGenerator(name = "catalogue_category_generator", sequenceName = "web_store.seq_catalogue_category_id", allocationSize = 1)
    @Column(name = "catalogue_category_id")
    private Integer catalogueCategoryId;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "catalogue_id", nullable = false)
    private Catalogue catalogue;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

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