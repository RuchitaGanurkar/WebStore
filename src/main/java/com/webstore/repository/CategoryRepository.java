package com.webstore.repository;

import com.webstore.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {
    boolean existsByCategoryName(String categoryName);

    @Query(value = "SELECT c.category_name FROM web_store.category c ORDER BY c.category_id ASC", nativeQuery = true)
    List<String> findTop3CategoryNames(); // Fetch first 3 names


}
