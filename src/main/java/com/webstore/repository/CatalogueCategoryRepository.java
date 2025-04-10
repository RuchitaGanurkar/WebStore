package com.webstore.repository;

import com.webstore.entity.CatalogueCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CatalogueCategoryRepository extends JpaRepository<CatalogueCategory, Integer> {
}
