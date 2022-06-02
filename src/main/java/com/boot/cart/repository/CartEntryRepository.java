package com.boot.cart.repository;

import com.boot.cart.model.CartEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;



@Repository
public interface CartEntryRepository extends JpaRepository<CartEntry, Long> {

}
