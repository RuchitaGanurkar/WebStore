package com.webstore.repository.cart;

import com.webstore.entity.cart.CartHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface CartHistoryRepository extends JpaRepository<CartHistory, Long> {

    List<CartHistory> findByCartCartId(Long cartId);

    List<CartHistory> findByCreatedBy(String createdBy);

    List<CartHistory> findByUpdatedBy(String updatedBy);

    @Query("SELECT ch FROM CartHistory ch WHERE ch.cart.cartId = :cartId ORDER BY ch.createdAt DESC")
    List<CartHistory> findByCartIdOrderByCreatedAtDesc(@Param("cartId") Long cartId);

    @Query("SELECT ch FROM CartHistory ch WHERE ch.createdAt BETWEEN :startDate AND :endDate")
    List<CartHistory> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                      @Param("endDate") LocalDateTime endDate);

    // ✅ Derived queries (no @Query needed)
    List<CartHistory> findByOldStatusStatusId(Integer statusId);

    List<CartHistory> findByNewStatusStatusId(Integer statusId);

    List<CartHistory> findByNewStatusStatusName(String statusName);
}

