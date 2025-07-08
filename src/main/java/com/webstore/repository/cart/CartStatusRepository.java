package com.webstore.repository.cart;

import com.webstore.entity.cart.CartStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartStatusRepository extends JpaRepository<CartStatus, Integer> {

    Optional<CartStatus> findByStatusName(CartStatus statusName);

    boolean existsByStatusName(CartStatus statusName);
}
