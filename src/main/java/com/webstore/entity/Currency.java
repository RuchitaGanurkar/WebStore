package com.webstore.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "currency", schema = "web_store")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Currency extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "currency_seq")
    @SequenceGenerator(name = "currency_seq", sequenceName = "web_store.seq_currency_id", allocationSize = 1)
    @Column(name = "currency_id")
    private Integer currencyId;

    @Column(name = "currency_code", nullable = false, length = 5, unique = true)
    private String currencyCode;

    @Column(name = "currency_name", nullable = false, length = 10)
    private String currencyName;

    @Column(name = "currency_symbol", nullable = false, length = 5)
    private String currencySymbol;
}
