package com.webstore.repository.product;

import com.webstore.entity.product.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    // Find User By Email
    Optional<User> findByEmail(String email);

    // Check Username Exist Or Not
    boolean existsByUsername(String username);

    // Check User Email Exist Or Not
    boolean existsByEmail(String email);
}