package com.webstore.repository.cart;

import com.webstore.entity.cart.CartProduct;
import com.webstore.entity.cart.CartProductStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface CartProductStatusRepository extends JpaRepository<CartProductStatus, Integer> {
    Optional<CartProductStatus> findByStatusName(String statusName);

    @Query("SELECT cp FROM CartProduct cp WHERE cp.cart.cartId = :cartId AND cp.status.statusName = :statusName")
    List<CartProduct> findByCartIdAndStatusName(@Param("cartId") Long cartId, @Param("statusName") String statusName);

    @Query("SELECT cp FROM CartProduct cp WHERE cp.cart.phoneNumber = :phoneNumber AND cp.status.statusName = :statusName")
    List<CartProduct> findByPhoneAndStatusName(@Param("phoneNumber") String phoneNumber, @Param("statusName") String statusName);

    @Query("SELECT COUNT(cp) FROM CartProduct cp WHERE cp.cart.cartId = :cartId AND cp.status.statusName = :statusName")
    Long countByCartIdAndStatusName(@Param("cartId") Long cartId, @Param("statusName") String statusName);

}