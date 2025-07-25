// CartRepository.java - Fixed version with correct property names
package com.webstore.repository.cart;

import com.webstore.entity.cart.Cart;
import com.webstore.entity.cart.CartStatus;
import com.webstore.enums.cart.CartStatusType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    List<Cart> findByPhoneNumber(String phoneNumber);

    List<Cart> findByPhoneNumberAndStatus(String phoneNumber, CartStatus status);

    // FIXED: Use catalogueCatalogueId instead of catalogueId
    List<Cart> findByCatalogueCatalogueId(Integer catalogueId);

    Optional<Cart> findByPhoneNumberAndStatusStatusName(String phoneNumber, CartStatus statusName);

    @Query("SELECT c FROM Cart c WHERE c.phoneNumber = :phoneNumber AND c.status.statusName = 'ACTIVE'")
    Optional<Cart> findActiveCartByPhoneNumber(@Param("phoneNumber") String phoneNumber);

    @Query("SELECT c FROM Cart c WHERE c.createdAt BETWEEN :startDate AND :endDate")
    List<Cart> findCartsByDateRange(@Param("startDate") LocalDateTime startDate,
                                    @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(c) FROM Cart c WHERE c.phoneNumber = :phoneNumber")
    Long countCartsByPhoneNumber(@Param("phoneNumber") String phoneNumber);

    List<Cart> findByStatusStatusName(CartStatusType statusName);

    // FIXED: Use catalogue.catalogueId instead of catalogue.id
    @Query("SELECT c FROM Cart c WHERE c.catalogue.catalogueId = :catalogueId AND c.status.statusName = 'ACTIVE'")
    List<Cart> findActiveCartsByCatalogueId(@Param("catalogueId") Integer catalogueId);

    @Query("SELECT c FROM Cart c WHERE c.phoneNumber = :phoneNumber AND c.catalogue.catalogueId = :catalogueId")
    List<Cart> findByPhoneNumberAndCatalogueId(@Param("phoneNumber") String phoneNumber,
                                               @Param("catalogueId") Integer catalogueId);

    @Query("SELECT c FROM Cart c WHERE c.phoneNumber = :phoneNumber AND c.catalogue.catalogueId = :catalogueId AND c.status.statusName = 'ACTIVE'")
    Optional<Cart> findActiveCartByPhoneNumberAndCatalogueId(@Param("phoneNumber") String phoneNumber,
                                                             @Param("catalogueId") Integer catalogueId);
}