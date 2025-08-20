package com.webstore.repository.cart;

import com.webstore.entity.cart.CartProduct;
import com.webstore.enums.cart.CartProductStatusType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartProductRepository extends JpaRepository<CartProduct, Long> {

    @Query("SELECT cp FROM CartProduct cp WHERE cp.cart.cartId = :cartId AND cp.status.statusName = :statusName")
    List<CartProduct> findByCartIdAndStatusName(@Param("cartId") Long cartId,
                                                @Param("statusName") CartProductStatusType statusName);

    @Query("SELECT cp FROM CartProduct cp WHERE cp.cart.phoneNumber = :phoneNumber AND cp.status.statusName = :statusName")
    List<CartProduct> findByPhoneAndStatusName(@Param("phoneNumber") String phoneNumber,
                                               @Param("statusName") CartProductStatusType statusName);

    @Query("SELECT COUNT(cp) FROM CartProduct cp WHERE cp.cart.cartId = :cartId AND cp.status.statusName = :statusName")
    Long countByCartIdAndStatusName(@Param("cartId") Long cartId,
                                    @Param("statusName") CartProductStatusType statusName);

    Optional<CartProduct> findByCart_CartIdAndProduct_ProductId(Long cartId, Long productId);
}
