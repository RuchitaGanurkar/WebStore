package com.webstore.entity;

import static com.webstore.DatabaseConstants.SCHEMA_NAME;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Table(name = "currency", schema = SCHEMA_NAME)
public class Currency extends BasicEntities {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "currency_generator")
    @SequenceGenerator(
            name = "currency_generator",
            sequenceName = SCHEMA_NAME + ".seq_currency_id",
            allocationSize = 1
    )
    @Column(name = "currency_id")
    private Integer currencyId;

    @Column(name = "currency_code", length = 5, nullable = false, unique = true)
    private String currencyCode;

    @Column(name = "currency_name", length = 10, nullable = false)
    private String currencyName;

    @Column(name = "currency_symbol", length = 5, nullable = false)
    private String currencySymbol;
}
