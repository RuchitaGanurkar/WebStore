package com.webstore.repository.cart;

import com.webstore.entity.cart.CartProductStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartProductStatusRepository extends JpaRepository<CartProductStatus, Integer> {

    Optional<CartProductStatus> findByStatusName(CartProductStatus statusName);

    boolean existsByStatusName(CartProductStatus statusName);
}