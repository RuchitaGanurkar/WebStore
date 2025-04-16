package com.webstore.entity;

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

import lombok.EqualsAndHashCode;
import lombok.Data;

@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Table(
        name = "catalogue_category",
        schema = "web_store",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_catalogue_category",
                columnNames = {"catalogue_id", "category_id"}
        )
)
public class CatalogueCategory extends BasicEntities {

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

}