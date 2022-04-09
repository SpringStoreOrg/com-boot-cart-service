package com.boot.cart.client;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import com.boot.cart.util.Constants;
import com.boot.services.dto.ProductDTO;

public class ProductServiceClient {

	@Autowired
	private RestTemplate productServiceRestTemplate;

	public List<ProductDTO> callGetAllProducts() {

		ProductDTO[] productArray = productServiceRestTemplate.getForEntity(Constants.GET_ALL_PRODUCTS, ProductDTO[].class)
				.getBody();

		return Arrays.asList(productArray);
	}

	public ProductDTO callGetProductByProductName(String productName) {

		return productServiceRestTemplate.getForEntity(Constants.GET_PRODUCT_BY_PRODUCT_NAME, ProductDTO.class)
				.getBody();
	}

	public void callUpdateProductByProductName(String productName, ProductDTO productDto) {
		productServiceRestTemplate.exchange(Constants.UPDATE_PRODUCT_BY_PRODUCT_NAME + productName, HttpMethod.PUT,
				new HttpEntity<>(productDto), String.class);
	}
}