package com.webstore.entity.product;

import static com.webstore.constant.DatabaseConstants.SCHEMA_NAME;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Table(name = "catalogue", schema = SCHEMA_NAME)
public class Catalogue extends BasicEntities {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "catalogue_generator")
    @SequenceGenerator(
            name = "catalogue_generator",
            sequenceName = SCHEMA_NAME + ".seq_catalogue_id",
            allocationSize = 1
    )
    @Column(name = "catalogue_id")
    private Integer catalogueId;

    @Column(name = "catalogue_name", length = 100, nullable = false, unique = true)
    private String catalogueName;

    @Column(name = "catalogue_description", length = 255)
    private String catalogueDescription;

    @OneToMany(
            mappedBy = "catalogue",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private Set<CatalogueCategory> catalogueCategories = new HashSet<>();
}
