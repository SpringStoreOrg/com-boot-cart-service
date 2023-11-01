package com.boot.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class CartEntryDTO{
	private Integer quantity;
	private String slug;
	private String name;
	private long price;
	private List<String> images;
}