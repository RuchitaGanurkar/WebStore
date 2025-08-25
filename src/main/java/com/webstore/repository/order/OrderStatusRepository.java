package com.webstore.repository.order;

import com.webstore.entity.order.OrderStatus;
import com.webstore.enums.order.OrderStatusType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderStatusRepository extends JpaRepository<OrderStatus, Integer> {

    @Query("SELECT os FROM OrderStatus os WHERE os.statusName = :statusName")
    Optional<OrderStatus> findByStatusName(@Param("statusName") OrderStatusType statusName);

    @Query("SELECT CASE WHEN COUNT(os) > 0 THEN true ELSE false END FROM OrderStatus os WHERE os.statusName = :statusName")
    boolean existsByStatusName(@Param("statusName") OrderStatusType statusName);
}