package com.webstore.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Entity
@Table(name = "catalogue_category", schema = "web_store")
@IdClass(CatalogueCategory.CatalogueCategoryId.class)
public class CatalogueCategory {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "catalogue_id", nullable = false)
    private Catalogue catalogue;

    @Id
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

    // Composite key class
    @Data
    @EqualsAndHashCode
    public static class CatalogueCategoryId implements Serializable {
        private static final long serialVersionUID = 1L;

        private Integer catalogue;
        private Integer category;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CatalogueCategory that = (CatalogueCategory) o;
        return Objects.equals(catalogue.getCatalogueId(), that.catalogue.getCatalogueId()) &&
                Objects.equals(category.getCategoryId(), that.category.getCategoryId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(catalogue.getCatalogueId(), category.getCategoryId());
    }
}