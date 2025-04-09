package com.webstore.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "catalogue", schema = "web_store")
public class Catalogue {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "catalogue_seq")
    @SequenceGenerator(name = "catalogue_seq", sequenceName = "web_store.seq_catalogue_id", allocationSize = 1)
    @Column(name = "catalogue_id")
    private Integer catalogueId;

    @Column(name = "catalogue_name", nullable = false, unique = true)
    private String catalogueName;

    @Column(name = "catalogue_description")
    private String catalogueDescription;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private String updatedBy;

    // Getters and Setters

    public Integer getCatalogueId() {
        return catalogueId;
    }

    public void setCatalogueId(Integer catalogueId) {
        this.catalogueId = catalogueId;
    }

    public String getCatalogueName() {
        return catalogueName;
    }

    public void setCatalogueName(String catalogueName) {
        this.catalogueName = catalogueName;
    }

    public String getCatalogueDescription() {
        return catalogueDescription;
    }

    public void setCatalogueDescription(String catalogueDescription) {
        this.catalogueDescription = catalogueDescription;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
}
