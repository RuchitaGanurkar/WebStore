package com.webstore.repository.cart;

import com.webstore.entity.cart.CartProductStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface CartProductStatusRepository extends JpaRepository<CartProductStatus, Integer> {

}