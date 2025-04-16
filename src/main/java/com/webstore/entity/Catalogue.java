package com.webstore.entity;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import lombok.EqualsAndHashCode;
import lombok.Data;

@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Table(name = "catalogue", schema = "web_store")
public class Catalogue extends BasicEntities {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "catalogue_generator")
    @SequenceGenerator(name = "catalogue_generator", sequenceName = "web_store.seq_catalogue_id", allocationSize = 1)
    @Column(name = "catalogue_id")
    private Integer catalogueId;

    @Column(name = "catalogue_name", length = 100, nullable = false, unique = true)
    private String catalogueName;

    @Column(name = "catalogue_description", length = 255)
    private String catalogueDescription;

    @OneToMany(mappedBy = "catalogue", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<CatalogueCategory> catalogueCategories = new HashSet<>();


}