package com.webstore.repository.product;

import com.webstore.entity.product.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    boolean existsByProductName(String productName);

    @Query("SELECT p FROM Product p WHERE p.category.categoryId = :categoryId")
    List<Product> findByCategoryId(@Param("categoryId") Integer categoryId);


    @Query("SELECT p FROM Product p WHERE p.category.categoryName = :categoryName")
    List<Product> findByCategoryName(@Param("categoryName") String categoryName);


    @Query("SELECT p FROM Product p WHERE p.category.categoryId = :categoryId ORDER BY p.createdAt DESC")
    List<Product> findTop5ByCategoryIdOrderByCreatedAtDesc(@Param("categoryId") Integer categoryId);


    @Query("SELECT p.productName FROM Product p WHERE p.category.categoryId = :categoryId ORDER BY p.productId ASC")
    List<String> findProductNamesByCategoryId(@Param("categoryId") Integer categoryId);


    @Query("SELECT p.productId FROM Product p WHERE p.productName = :productName")
    Integer findProductIdByProductName(@Param("productName") String productName);
}
