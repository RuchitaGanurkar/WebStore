package com.webstore.entity.product;

import static com.webstore.constant.DatabaseConstants.SCHEMA_NAME;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Table(
        name = "catalogue_category",
        schema = SCHEMA_NAME,
        uniqueConstraints = @UniqueConstraint(
                name = "uq_catalogue_category",
                columnNames = {"catalogue_id", "category_id"}
        )
)
public class CatalogueCategory extends BasicEntities {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "catalogue_category_generator")
    @SequenceGenerator(
            name = "catalogue_category_generator",
            sequenceName = SCHEMA_NAME + ".seq_catalogue_category_id",
            allocationSize = 1
    )
    @Column(name = "catalogue_category_id")
    private Integer catalogueCategoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "catalogue_id", nullable = false)
    private Catalogue catalogue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
}
