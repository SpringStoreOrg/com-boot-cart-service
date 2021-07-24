package com.boot.cart.repository;

import java.time.LocalDateTime;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.boot.services.model.Cart;
import com.boot.services.model.User;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

	Cart findByUser(User user);

	Set<Cart> findByLastUpdatedOnBefore(LocalDateTime date);
}
