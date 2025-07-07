package com.webstore.repository.product;

import com.webstore.entity.product.Currency;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CurrencyRepository extends JpaRepository<Currency, Integer> {

    Optional<Currency> findByCurrencyCode(String currencyCode);

    boolean existsByCurrencyCode(String currencyCode);

}
