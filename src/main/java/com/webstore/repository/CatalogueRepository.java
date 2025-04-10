package com.webstore.repository;

import com.webstore.entity.Catalogue;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CatalogueRepository extends JpaRepository<Catalogue, Integer> {
}
