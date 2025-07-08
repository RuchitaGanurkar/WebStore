package com.webstore.repository.cart;

import com.webstore.entity.cart.CartProduct;
import com.webstore.entity.cart.CartProductStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface CartProductRepository extends JpaRepository<CartProduct, Long> {

    List<CartProduct> findByCartCartId(Long cartId);

    List<CartProduct> findByStatus(CartProductStatus status);

    List<CartProduct> findByCartCartIdAndStatus(Long cartId, CartProductStatus status);

    @Query("SELECT cp FROM CartProduct cp WHERE cp.cart.cartId = :cartId AND cp.status.statusName = 'ADDED'")
    List<CartProduct> findActiveProductsByCartId(@Param("cartId") Long cartId);

    @Query("SELECT cp FROM CartProduct cp WHERE cp.cart.phoneNumber = :phoneNumber AND cp.status.statusName = 'ADDED'")
    List<CartProduct> findActiveProductsByPhoneNumber(@Param("phoneNumber") String phoneNumber);

    @Query("SELECT COUNT(cp) FROM CartProduct cp WHERE cp.cart.cartId = :cartId AND cp.status.statusName = 'ADDED'")
    Long countActiveProductsInCart(@Param("cartId") Long cartId);

}