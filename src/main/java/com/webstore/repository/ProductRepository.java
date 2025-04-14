package com.webstore.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.webstore.entity.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
}