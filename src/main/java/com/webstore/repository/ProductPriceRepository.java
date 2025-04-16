package com.webstore.repository;


import com.webstore.entity.ProductPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductPriceRepository extends JpaRepository<ProductPrice, Integer> {

        List<ProductPrice> findByProductProductId(Integer productId);

        Optional<ProductPrice> findByProductProductIdAndCurrencyCurrencyId(Integer productId, Integer currencyId);

        @Query("SELECT pp FROM ProductPrice pp JOIN FETCH pp.product p JOIN FETCH pp.currency c WHERE p.category.categoryId = :categoryId")
        List<ProductPrice> findByProductCategoryCategoryId(@Param("categoryId") Integer categoryId);

        @Query("SELECT pp FROM ProductPrice pp JOIN FETCH pp.product p JOIN FETCH pp.currency c WHERE p.productId IN :productIds")
        List<ProductPrice> findByProductProductIdIn(@Param("productIds") List<Integer> productIds);

        @Query("SELECT pp FROM ProductPrice pp JOIN FETCH pp.product p JOIN FETCH pp.currency c WHERE pp.currency.currencyId = :currencyId")
        List<ProductPrice> findByCurrencyCurrencyId(@Param("currencyId") Integer currencyId);
    }

