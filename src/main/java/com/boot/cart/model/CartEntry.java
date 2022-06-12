package com.boot.cart.model;

import com.boot.cart.client.ProductServiceClient;
import com.boot.cart.dto.CartDTO;
import com.boot.cart.dto.CartEntryDTO;
import com.boot.cart.dto.ProductDTO;
import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "cart_entry")
public class CartEntry  {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column
    private Long id;

    @Column
    private String productName;

    @Column
    private double price;

    @Column
    private Integer quantity;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "cart_id")
    private Cart cart;




}
