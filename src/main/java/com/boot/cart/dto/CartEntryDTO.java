package com.boot.cart.dto;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;


@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class CartEntryDTO {

	private long id;

	private String productName;

	private String description;

	private double price;

	private String photoLink;

	private Integer stock;

	private Integer quantity;


}