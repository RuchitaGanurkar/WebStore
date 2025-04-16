package com.webstore.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import lombok.EqualsAndHashCode;
import lombok.Data;

@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Table(name = "currency", schema = "web_store")
public class Currency extends BasicEntities {

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

}