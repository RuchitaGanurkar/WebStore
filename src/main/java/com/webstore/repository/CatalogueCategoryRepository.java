package com.webstore.repository;

import com.webstore.entity.CatalogueCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CatalogueCategoryRepository extends JpaRepository<CatalogueCategory, Integer> {

    Optional<CatalogueCategory> findByCatalogueCatalogueIdAndCategoryCategoryId(Integer catalogueId, Integer categoryId);

    boolean existsByCatalogueCatalogueIdAndCategoryCategoryId(Integer catalogueId, Integer categoryId);
}
