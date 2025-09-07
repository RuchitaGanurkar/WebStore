package com.webstore.repository.order;

import com.webstore.entity.order.Order;
import com.webstore.enums.order.OrderStatusType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // Find orders by cart ID
    @Query("SELECT o FROM Order o WHERE o.cart.cartId = :cartId")
    List<Order> findByCartId(@Param("cartId") Long cartId);

    // Find orders by phone number (through cart)
    @Query("SELECT o FROM Order o WHERE o.cart.phoneNumber = :phoneNumber")
    List<Order> findByPhoneNumber(@Param("phoneNumber") String phoneNumber);

    // Find orders by phone number and status
    @Query("SELECT o FROM Order o WHERE o.cart.phoneNumber = :phoneNumber AND o.status.statusName = :statusName")
    List<Order> findByPhoneNumberAndStatus(@Param("phoneNumber") String phoneNumber,
                                           @Param("statusName") OrderStatusType statusName);

    // Find orders by status
    @Query("SELECT o FROM Order o WHERE o.status.statusName = :statusName")
    List<Order> findByStatus(@Param("statusName") OrderStatusType status);

    // Find orders by status with pagination
    @Query("SELECT o FROM Order o WHERE o.status.statusName = :statusName")
    Page<Order> findByStatus(@Param("statusName") OrderStatusType statusName, Pageable pageable);

    // Find orders by phone number with pagination
    @Query("SELECT o FROM Order o WHERE o.cart.phoneNumber = :phoneNumber")
    Page<Order> findByPhoneNumber(@Param("phoneNumber") String phoneNumber, Pageable pageable);

    // Find orders within date range
    @Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate")
    List<Order> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate);

    // Find orders by total amount range
    @Query("SELECT o FROM Order o WHERE o.totalAmount BETWEEN :minAmount AND :maxAmount")
    List<Order> findByTotalAmountBetween(@Param("minAmount") BigDecimal minAmount,
                                         @Param("maxAmount") BigDecimal maxAmount);

    // Count orders by status
    @Query("SELECT COUNT(o) FROM Order o WHERE o.status.statusName = :statusName")
    Long countByStatus(@Param("statusName") OrderStatusType statusName);

    // Count orders by phone number
    @Query("SELECT COUNT(o) FROM Order o WHERE o.cart.phoneNumber = :phoneNumber")
    Long countByPhoneNumber(@Param("phoneNumber") String phoneNumber);

    // Find latest order by phone number
    @Query("SELECT o FROM Order o WHERE o.cart.phoneNumber = :phoneNumber ORDER BY o.createdAt DESC")
    List<Order> findLatestOrderByPhoneNumber(@Param("phoneNumber") String phoneNumber, Pageable pageable);

    // Get total amount by phone number
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.cart.phoneNumber = :phoneNumber")
    BigDecimal getTotalAmountByPhoneNumber(@Param("phoneNumber") String phoneNumber);

    // Find orders by multiple statuses
    @Query("SELECT o FROM Order o WHERE o.status.statusName IN :statusNames")
    List<Order> findByStatusIn(@Param("statusNames") List<OrderStatusType> statusNames);

    // Check if order exists by cart ID
    @Query("SELECT CASE WHEN COUNT(o) > 0 THEN true ELSE false END FROM Order o WHERE o.cart.cartId = :cartId")
    boolean existsByCartId(@Param("cartId") Long cartId);
}