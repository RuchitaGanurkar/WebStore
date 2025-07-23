package com.webstore.repository.cart;

import com.webstore.entity.cart.CartProductStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartProductStatusRepository extends JpaRepository<CartProductStatus, Integer> {

    Optional<CartProductStatus> findByStatusName(String statusName);
}
