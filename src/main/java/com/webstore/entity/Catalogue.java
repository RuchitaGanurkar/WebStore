package com.webstore.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "catalogue", schema = "web_store")
@Data // Generates getters, setters, toString, equals, and hashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder // Optional: for builder pattern
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

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private String createdBy;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private String updatedBy;
}
