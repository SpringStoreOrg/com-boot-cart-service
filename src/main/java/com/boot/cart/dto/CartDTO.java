package com.boot.cart.dto;

import com.boot.cart.model.CartEntry;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class CartDTO {

	private long id;

	private Long userId;

	private double total;

	private List<CartEntry> entries;
}
