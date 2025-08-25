package com.webstore.repository.order;

import com.webstore.entity.order.OrderHistory;
import com.webstore.enums.order.OrderStatusType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderHistoryRepository extends JpaRepository<OrderHistory, Long> {

    // Find all history by order ID
    @Query("SELECT oh FROM OrderHistory oh WHERE oh.order.orderId = :orderId ORDER BY oh.createdAt DESC")
    List<OrderHistory> findByOrderIdOrderByCreatedAtDesc(@Param("orderId") Long orderId);

    // Find history by order ID with pagination
    @Query("SELECT oh FROM OrderHistory oh WHERE oh.order.orderId = :orderId ORDER BY oh.createdAt DESC")
    Page<OrderHistory> findByOrderIdOrderByCreatedAtDesc(@Param("orderId") Long orderId, Pageable pageable);

    // Find history by phone number (through order -> cart)
    @Query("SELECT oh FROM OrderHistory oh WHERE oh.order.cart.phoneNumber = :phoneNumber ORDER BY oh.createdAt DESC")
    List<OrderHistory> findByPhoneNumberOrderByCreatedAtDesc(@Param("phoneNumber") String phoneNumber);

    // Find history by new status
    @Query("SELECT oh FROM OrderHistory oh WHERE oh.newStatus.statusName = :statusName ORDER BY oh.createdAt DESC")
    List<OrderHistory> findByNewStatusOrderByCreatedAtDesc(@Param("statusName") OrderStatusType statusName);

    // Find history by old status
    @Query("SELECT oh FROM OrderHistory oh WHERE oh.oldStatus.statusName = :statusName ORDER BY oh.createdAt DESC")
    List<OrderHistory> findByOldStatusOrderByCreatedAtDesc(@Param("statusName") OrderStatusType statusName);

    // Find status transitions (old -> new)
    @Query("SELECT oh FROM OrderHistory oh WHERE oh.oldStatus.statusName = :oldStatus AND oh.newStatus.statusName = :newStatus ORDER BY oh.createdAt DESC")
    List<OrderHistory> findByStatusTransition(@Param("oldStatus") OrderStatusType oldStatus,
                                              @Param("newStatus") OrderStatusType newStatus);

    // Find history within date range
    @Query("SELECT oh FROM OrderHistory oh WHERE oh.createdAt BETWEEN :startDate AND :endDate ORDER BY oh.createdAt DESC")
    List<OrderHistory> findByCreatedAtBetweenOrderByCreatedAtDesc(@Param("startDate") LocalDateTime startDate,
                                                                  @Param("endDate") LocalDateTime endDate);

    // Count status changes by order
    @Query("SELECT COUNT(oh) FROM OrderHistory oh WHERE oh.order.orderId = :orderId")
    Long countByOrderId(@Param("orderId") Long orderId);

    // Find the latest status change by order
    @Query("SELECT oh FROM OrderHistory oh WHERE oh.order.orderId = :orderId ORDER BY oh.createdAt DESC")
    List<OrderHistory> findLatestByOrderId(@Param("orderId") Long orderId, Pageable pageable);

    // Find history by created by
    @Query("SELECT oh FROM OrderHistory oh WHERE oh.createdBy = :createdBy ORDER BY oh.createdAt DESC")
    List<OrderHistory> findByCreatedByOrderByCreatedAtDesc(@Param("createdBy") String createdBy);

    // Check if specific status transition exists
    @Query("SELECT CASE WHEN COUNT(oh) > 0 THEN true ELSE false END FROM OrderHistory oh WHERE oh.order.orderId = :orderId AND oh.oldStatus.statusName = :oldStatus AND oh.newStatus.statusName = :newStatus")
    boolean existsByOrderIdAndStatusTransition(@Param("orderId") Long orderId,
                                               @Param("oldStatus") OrderStatusType oldStatus,
                                               @Param("newStatus") OrderStatusType newStatus);
}