package com.webstore.repository.product;

import com.webstore.entity.product.CatalogueCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CatalogueCategoryRepository extends JpaRepository<CatalogueCategory, Integer> {

    Optional<CatalogueCategory> findByCatalogueCatalogueIdAndCategoryCategoryId(Integer catalogueId, Integer categoryId);

    boolean existsByCatalogueCatalogueIdAndCategoryCategoryId(Integer catalogueId, Integer categoryId);
}
