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

    Optional<OrderStatus> findByStatusName(OrderStatusType statusName);

    boolean existsByStatusName(OrderStatusType statusName);
}