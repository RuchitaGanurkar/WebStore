package com.webstore.repository.cart;

import com.webstore.entity.cart.CartProductHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface CartProductHistoryRepository extends JpaRepository<CartProductHistory, Long> {

    List<CartProductHistory> findByCartProductCartProductId(Long cartProductId);

    List<CartProductHistory> findByProductId(Integer productId);

    List<CartProductHistory> findByCreatedBy(String createdBy);

    List<CartProductHistory> findByUpdatedBy(String updatedBy);

    @Query("SELECT cph FROM CartProductHistory cph WHERE cph.cartProduct.cartProductId = :cartProductId ORDER BY cph.createdAt DESC")
    List<CartProductHistory> findByCartProductIdOrderByCreatedAtDesc(@Param("cartProductId") Long cartProductId);

    @Query("SELECT cph FROM CartProductHistory cph WHERE cph.productId = :productId AND cph.createdAt BETWEEN :startDate AND :endDate")
    List<CartProductHistory> findByProductIdAndDateRange(@Param("productId") Integer productId,
                                                         @Param("startDate") LocalDateTime startDate,
                                                         @Param("endDate") LocalDateTime endDate);

    @Query("SELECT cph FROM CartProductHistory cph WHERE cph.cartProduct.cart.phoneNumber = :phoneNumber ORDER BY cph.createdAt DESC")
    List<CartProductHistory> findByPhoneNumberOrderByCreatedAtDesc(@Param("phoneNumber") String phoneNumber);

    @Query("SELECT SUM(cph.newQuantity - cph.oldQuantity) FROM CartProductHistory cph WHERE cph.productId = :productId")
    Long getTotalQuantityChangeForProduct(@Param("productId") Integer productId);
}