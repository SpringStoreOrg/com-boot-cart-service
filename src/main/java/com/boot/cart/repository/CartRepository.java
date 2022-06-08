package com.boot.cart.repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import com.boot.cart.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {


    Set<Cart> findByLastUpdatedOnBefore(LocalDateTime date);

    Optional<Cart> findByUserId(long userId);
}
