package com.boot.cart.dto;


import com.boot.cart.enums.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {

	private long id;

	@Size(min = 3, message = "Min Product name size is 3 characters!")
	@Size(max = 30, message = "Max Product name size is 30 characters!")
	private String name;

	@Size(min = 3,message = "Min Product description size is 3 characters!")
	@Size(max = 600,message = "Max Product description size is 600 characters!")
	private String description;

	@Positive(message = "Product price should be positive number!")
	private double price;

	@Size(min = 3,message = "Min Product photo link size is 3 characters!")
	@Size(max = 600,message = "Max Product photo link size is 600 characters!")
	private String photoLink;

	@Size(min = 3, message = "Min category name size is 3 characters!")
	@Size(max = 30, message = "Max category name size is 30 characters!")
	private String category;

	@Positive(message = "Product stock should be positive number!")
	private int stock;

	private int quantity;

	@Enumerated(EnumType.STRING)
	@Column
	private ProductStatus status;
}