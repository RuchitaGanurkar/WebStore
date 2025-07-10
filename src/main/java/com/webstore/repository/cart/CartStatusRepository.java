package com.webstore.repository.cart;

import com.webstore.entity.cart.CartStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface CartStatusRepository extends JpaRepository<CartStatus, Integer> {

}
