package com.boot.cart.dto;


import com.boot.cart.enums.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import java.util.List;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
	private long id;
	private String name;
	private String description;
	private double price;
	private List<PhotoDTO> photoLinks;
	private String category;
	private int stock;
	private int quantity;
	private ProductStatus status;
}