package com.webstore.repository.cart;

import com.webstore.entity.cart.CartStatus;
import com.webstore.enums.cart.CartStatusType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface CartStatusRepository extends JpaRepository<CartStatus, Integer> {

    boolean existsByStatusName(CartStatusType statusName);

    boolean existsByStatusNameAndStatusIdNot(CartStatusType statusName, Integer statusId);

    Optional<CartStatus> findByStatusName(CartStatusType statusName);

    @Query("SELECT COUNT(c) FROM CartStatus c WHERE c.statusId = :statusId")
    long countCartsUsingStatus(@Param("statusId") Integer statusId);

}
