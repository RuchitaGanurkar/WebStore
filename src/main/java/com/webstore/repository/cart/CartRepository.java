package com.webstore.repository.cart;

import com.webstore.entity.cart.Cart;
import com.webstore.entity.cart.CartStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    List<Cart> findByPhoneNumber(String phoneNumber);

    List<Cart> findByPhoneNumberAndStatus(String phoneNumber, CartStatus status);

    List<Cart> findByCatalogueCategoryId(Integer catalogueCategoryId);

    Optional<Cart> findByPhoneNumberAndStatusStatusName(String phoneNumber, CartStatus statusName);

    @Query("SELECT c FROM Cart c WHERE c.phoneNumber = :phoneNumber AND c.status.statusName = 'ACTIVE'")
    Optional<Cart> findActiveCartByPhoneNumber(@Param("phoneNumber") String phoneNumber);

    @Query("SELECT c FROM Cart c WHERE c.createdAt BETWEEN :startDate AND :endDate")
    List<Cart> findCartsByDateRange(@Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(c) FROM Cart c WHERE c.phoneNumber = :phoneNumber")
    Long countCartsByPhoneNumber(@Param("phoneNumber") String phoneNumber);

    List<Cart> findByStatusStatusName(CartStatus statusName);
}