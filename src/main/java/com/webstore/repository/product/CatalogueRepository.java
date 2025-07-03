package com.webstore.repository.product;

import com.webstore.entity.product.Catalogue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CatalogueRepository extends JpaRepository<Catalogue, Integer> {
    List<Catalogue> findByCatalogueNameContainingIgnoreCase(String name);
//    List<Catalogue> findByCatalogueDescriptionContainingIgnoreCase(String description);
}
